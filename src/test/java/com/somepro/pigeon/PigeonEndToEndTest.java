package com.somepro.pigeon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 赛鸽训放后半条线端到端验证（H2 MySQL 兼容模式，无需 Docker/MySQL）：
 * 报到 → 出成绩 → 名次榜全链路，含重复报到、时间窗、重算覆盖、分页、榜与库一致。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // 强制 H2：沙箱环境注入了 SPRING_DATASOURCE_URL（MySQL），此处测试属性优先级更高
                "spring.datasource.url=jdbc:h2:mem:pigeon;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:schema-h2.sql",
                "pagehelper.helper-dialect=h2",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("h2")
class PigeonEndToEndTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime RELEASE = LocalDateTime.parse("2026-09-23T07:00:00");
    private static final LocalDateTime CLOSE = LocalDateTime.parse("2026-09-23T18:00:00");

    @Autowired
    private WebTestClient web;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private long raceId = 1001L;

    /** 全路由需认证（脚手架 SecurityConfig），测试用 HTTP Basic admin/admin123。 */
    private static final String BASIC = "Basic " + Base64.getEncoder()
            .encodeToString("admin:admin123".getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void seed() {
        jdbc.execute("DROP ALL OBJECTS");
        // 用 Spring 的 sql init 不会在方法间重跑，这里直接手动建表+塞数
        seedSchemaAndData();
    }

    private void seedSchemaAndData() {
        jdbc.execute("""
                CREATE TABLE t_band (
                    id BIGINT NOT NULL PRIMARY KEY, band_code VARCHAR(32) NOT NULL,
                    band_year INT NOT NULL, owner_name VARCHAR(64) NOT NULL,
                    loft_city VARCHAR(64), status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
                    del_flag TINYINT NOT NULL DEFAULT 0,
                    create_by VARCHAR(64), create_time DATETIME,
                    update_by VARCHAR(64), update_time DATETIME,
                    CONSTRAINT uk_band_code UNIQUE (band_code))
                """);
        jdbc.execute("""
                CREATE TABLE t_race (
                    id BIGINT NOT NULL PRIMARY KEY, race_code VARCHAR(32) NOT NULL,
                    title VARCHAR(128) NOT NULL, release_site VARCHAR(128) NOT NULL,
                    release_at DATETIME NOT NULL, close_at DATETIME,
                    distance_km DECIMAL(8,3) NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
                    del_flag TINYINT NOT NULL DEFAULT 0,
                    create_by VARCHAR(64), create_time DATETIME,
                    update_by VARCHAR(64), update_time DATETIME,
                    CONSTRAINT uk_race_code UNIQUE (race_code))
                """);
        jdbc.execute("""
                CREATE TABLE t_entry (
                    id BIGINT NOT NULL PRIMARY KEY, race_id BIGINT NOT NULL, band_id BIGINT NOT NULL,
                    basket_no VARCHAR(16), entry_time DATETIME,
                    del_flag TINYINT NOT NULL DEFAULT 0,
                    create_by VARCHAR(64), create_time DATETIME,
                    update_by VARCHAR(64), update_time DATETIME,
                    CONSTRAINT uk_race_band UNIQUE (race_id, band_id))
                """);
        jdbc.execute("""
                CREATE TABLE t_clocking (
                    id BIGINT NOT NULL PRIMARY KEY, entry_id BIGINT NOT NULL, race_id BIGINT NOT NULL,
                    clock_at DATETIME NOT NULL, source VARCHAR(16) NOT NULL DEFAULT 'SCAN',
                    del_flag TINYINT NOT NULL DEFAULT 0,
                    create_by VARCHAR(64), create_time DATETIME,
                    update_by VARCHAR(64), update_time DATETIME,
                    CONSTRAINT uk_entry UNIQUE (entry_id))
                """);
        jdbc.execute("""
                CREATE TABLE t_result (
                    id BIGINT NOT NULL PRIMARY KEY, race_id BIGINT NOT NULL, entry_id BIGINT NOT NULL,
                    speed_mpm DECIMAL(12,2) NOT NULL, rank_no INT,
                    del_flag TINYINT NOT NULL DEFAULT 0,
                    create_by VARCHAR(64), create_time DATETIME,
                    update_by VARCHAR(64), update_time DATETIME,
                    CONSTRAINT uk_race_entry UNIQUE (race_id, entry_id))
                """);

        jdbc.update("INSERT INTO t_race(id, race_code, title, release_site, release_at, close_at, distance_km, status) "
                        + "VALUES (?, 'XF-2026-018', '9月23日300公里', '鹤壁', ?, ?, 300.000, 'RELEASED')",
                raceId, RELEASE, CLOSE);
        // 4 羽在赛鸽 + 1 羽停赛鸽；3 羽集了鸽
        insertBand(201L, "CHN2026-A-000201", "张三");
        insertBand(202L, "CHN2026-A-000202", "李四");
        insertBand(203L, "CHN2026-A-000203", "王五");
        insertBand(204L, "CHN2026-A-000204", "赵六");
        insertBand(205L, "CHN2026-A-000205", "钱七", "SUSPENDED");
        insertEntry(301L, raceId, 201L);
        insertEntry(302L, raceId, 202L);
        insertEntry(303L, raceId, 203L);
        // 205 停赛但也造了历史集鸽（验证状态拦截）
        insertEntry(305L, raceId, 205L);
        // 另一场赛，验证串场
        jdbc.update("INSERT INTO t_race(id, race_code, title, release_site, release_at, distance_km, status) "
                + "VALUES (1002, 'XF-2026-019', '另一场', '新乡', ?, 200.000, 'RELEASED')", RELEASE);
    }

    private void insertBand(long id, String code, String owner) {
        insertBand(id, code, owner, "ACTIVE");
    }

    private void insertBand(long id, String code, String owner, String status) {
        jdbc.update("INSERT INTO t_band(id, band_code, band_year, owner_name, loft_city, status) "
                + "VALUES (?, ?, 2026, ?, '北京', ?)", id, code, owner, status);
    }

    private void insertEntry(long id, long raceId, long bandId) {
        jdbc.update("INSERT INTO t_entry(id, race_id, band_id, basket_no) VALUES (?, ?, ?, ?)",
                id, raceId, bandId, "B" + id);
    }

    private String reqJson(String bandCode, LocalDateTime clockAt, String source) throws Exception {
        return om.writeValueAsString(java.util.Map.of(
                "raceId", raceId,
                "bandCode", bandCode,
                "clockAt", FMT.format(clockAt),
                "source", source));
    }

    private JsonNode postClock(String bandCode, LocalDateTime clockAt, String source) throws Exception {
        String resp = web.post().uri("/api/pigeon/clocking")
                .header("Authorization", BASIC)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reqJson(bandCode, clockAt, source))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        return om.readTree(resp);
    }

    private JsonNode postScore() throws Exception {
        String resp = web.post().uri("/api/pigeon/races/" + raceId + "/score")
                .header("Authorization", BASIC)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        return om.readTree(resp);
    }

    private JsonNode getRank(int pageNum, int pageSize) throws Exception {
        String resp = web.get().uri("/api/pigeon/races/" + raceId + "/rank?pageNum=" + pageNum + "&pageSize=" + pageSize)
                .header("Authorization", BASIC)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        return om.readTree(resp);
    }

    @Test
    void fullFlow_clockIn_score_rank_consistentWithDb() throws Exception {
        // 1. 三羽先后归巢：李四最快（4h），张三次之（5h），王五最晚（6h）
        JsonNode ok1 = postClock("CHN2026-A-000202", RELEASE.plusHours(4), "SCAN");
        assertEquals(0, ok1.get("code").asInt(), ok1.toString());
        assertEquals("SCAN", ok1.get("data").get("source").asText());
        JsonNode ok2 = postClock("CHN2026-A-000201", RELEASE.plusHours(5), "MANUAL");
        assertEquals(0, ok2.get("code").asInt());
        assertEquals("MANUAL", ok2.get("data").get("source").asText());
        JsonNode ok3 = postClock("CHN2026-A-000203", RELEASE.plusHours(6), "scan");
        assertEquals(0, ok3.get("code").asInt());

        // t_clocking 落了 3 行
        assertEquals(Integer.valueOf(3), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_clocking WHERE race_id = ?", Integer.class, raceId));

        // 2. 重复报到被打回
        JsonNode dup = postClock("CHN2026-A-000202", RELEASE.plusHours(4).plusMinutes(5), "SCAN");
        assertEquals(1, dup.get("code").asInt());
        assertTrue(dup.get("msg").asText().contains("已报过到"), dup.get("msg").asText());
        // 库里没多
        assertEquals(Integer.valueOf(3), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_clocking WHERE race_id = ?", Integer.class, raceId));

        // 3. 赵六 204 先补集鸽，再测时间窗
        jdbc.update("INSERT INTO t_entry(id, race_id, band_id, basket_no) VALUES (304, ?, 204, 'B304')",
                raceId);
        // 3a. 早于开笼
        JsonNode early = postClock("CHN2026-A-000204", RELEASE.minusMinutes(1), "SCAN");
        assertEquals(1, early.get("code").asInt());
        assertTrue(early.get("msg").asText().contains("开笼"));

        // 3b. 与开笼同一时刻也算无效
        JsonNode atRelease = postClock("CHN2026-A-000204", RELEASE, "SCAN");
        assertEquals(1, atRelease.get("code").asInt());
        assertTrue(atRelease.get("msg").asText().contains("开笼"));

        // 4. 晚于关门
        JsonNode late = postClock("CHN2026-A-000204", CLOSE.plusSeconds(1), "SCAN");
        assertEquals(1, late.get("code").asInt());
        assertTrue(late.get("msg").asText().contains("关门"));
        // 踩点关门有效
        JsonNode onClose = postClock("CHN2026-A-000204", CLOSE, "SCAN");
        assertEquals(0, onClose.get("code").asInt());

        // 5. 未集鸽（换一场没集鸽的赛）
        String resp = web.post().uri("/api/pigeon/clocking")
                .header("Authorization", BASIC)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(om.writeValueAsString(java.util.Map.of(
                        "raceId", 1002, "bandCode", "CHN2026-A-000201",
                        "clockAt", FMT.format(RELEASE.plusHours(5)), "source", "SCAN")))
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        JsonNode notEntered = om.readTree(resp);
        assertEquals(1, notEntered.get("code").asInt());
        assertTrue(notEntered.get("msg").asText().contains("没有集鸽记录"));

        // 6. 停赛足环
        JsonNode suspended = postClock("CHN2026-A-000205", RELEASE.plusHours(5), "SCAN");
        assertEquals(1, suspended.get("code").asInt());
        assertTrue(suspended.get("msg").asText().contains("SUSPENDED"));

        // 7. 不存在的足环 / 赛项 / 非法来源
        assertEquals(1, postClock("CHN2099-X-000000", RELEASE.plusHours(5), "SCAN").get("code").asInt());
        String badRace = web.post().uri("/api/pigeon/clocking")
                .header("Authorization", BASIC)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(om.writeValueAsString(java.util.Map.of(
                        "raceId", 9999, "bandCode", "CHN2026-A-000201",
                        "clockAt", FMT.format(RELEASE.plusHours(5)), "source", "SCAN")))
                .exchange().expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        assertEquals(1, om.readTree(badRace).get("code").asInt());
        // 非法来源：用一只集过鸽但尚未报到的足环（否则会先撞重复校验）
        jdbc.update("INSERT INTO t_band(id, band_code, band_year, owner_name, status) "
                + "VALUES (207, 'CHN2026-A-000207', 2026, '周九', 'ACTIVE')");
        jdbc.update("INSERT INTO t_entry(id, race_id, band_id, basket_no) VALUES (307, ?, 207, 'B307')",
                raceId);
        JsonNode badSource = postClock("CHN2026-A-000207", RELEASE.plusHours(7), "CARRIER_PIGEON");
        assertEquals(1, badSource.get("code").asInt());
        // 非法枚举在接口层 @Pattern 即被拦下
        assertTrue(badSource.get("msg").asText().contains("报到来源只支持"),
                badSource.get("msg").asText());

        // 8. 出成绩
        JsonNode score = postScore();
        assertEquals(0, score.get("code").asInt(), score.toString());
        JsonNode rows = score.get("data");
        assertEquals(4, rows.size()); // 202/201/203/204
        // 名次与分速：300km
        // 4h=240min → 1250.00；5h=300min → 1000.00；6h=360min → 833.33；11h=660min → 454.55
        checkResultRow(rows.get(0), 302L, "1250.00", 1);
        checkResultRow(rows.get(1), 301L, "1000.00", 2);
        checkResultRow(rows.get(2), 303L, "833.33", 3);
        checkResultRow(rows.get(3), 304L, "454.55", 4);

        // 9. 重算覆盖：库里仍只有 4 行，不产生两套
        JsonNode score2 = postScore();
        assertEquals(0, score2.get("code").asInt());
        assertEquals(Integer.valueOf(4), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_result WHERE race_id = ?", Integer.class, raceId));
        assertEquals(Integer.valueOf(1), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_result WHERE race_id = ? AND entry_id = 302 AND rank_no = 1",
                Integer.class, raceId));

        // 10. 新增一羽更快的归巢后重算：名次整体重排
        jdbc.update("INSERT INTO t_band(id, band_code, band_year, owner_name, status) "
                + "VALUES (206, 'CHN2026-A-000206', 2026, '孙八', 'ACTIVE')");
        jdbc.update("INSERT INTO t_entry(id, race_id, band_id, basket_no) VALUES (306, ?, 206, 'B306')",
                raceId);
        JsonNode fastest = postClock("CHN2026-A-000206", RELEASE.plusHours(3), "SCAN");
        assertEquals(0, fastest.get("code").asInt());
        JsonNode score3 = postScore();
        JsonNode rows3 = score3.get("data");
        assertEquals(5, rows3.size());
        checkResultRow(rows3.get(0), 306L, "1666.67", 1);
        checkResultRow(rows3.get(1), 302L, "1250.00", 2);
        assertEquals(Integer.valueOf(5), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_result WHERE race_id = ?", Integer.class, raceId));
        // 旧的 rank_no=1 归属已经被覆盖
        assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_result WHERE race_id = ? AND entry_id = 302 AND rank_no = 1",
                Integer.class, raceId));

        // 11. 名次榜：内容与库一致
        JsonNode rank = getRank(1, 3);
        assertEquals(0, rank.get("code").asInt());
        assertEquals(5, rank.get("data").get("total").asInt());
        assertEquals(2, rank.get("data").get("totalPages").asInt());
        JsonNode page1 = rank.get("data").get("content");
        assertEquals(3, page1.size());
        checkRankRow(page1.get(0), "CHN2026-A-000206", "孙八", "1666.67", 1);
        checkRankRow(page1.get(1), "CHN2026-A-000202", "李四", "1250.00", 2);
        checkRankRow(page1.get(2), "CHN2026-A-000201", "张三", "1000.00", 3);
        assertEquals("2026-09-23 12:00:00",
                page1.get(2).get("clockAt").asText());

        JsonNode page2 = getRank(2, 3);
        JsonNode content2 = page2.get("data").get("content");
        assertEquals(2, content2.size());
        checkRankRow(content2.get(0), "CHN2026-A-000203", "王五", "833.33", 4);
        checkRankRow(content2.get(1), "CHN2026-A-000204", "赵六", "454.55", 5);

        // 越界页空
        JsonNode page9 = getRank(9, 3);
        assertEquals(0, page9.get("data").get("content").size());

        // 榜数与库交叉核对：分速、名次逐条比对 t_result
        JsonNode boardAll = getRank(1, 100).get("data").get("content");
        jdbc.query("SELECT r.rank_no, r.speed_mpm, b.band_code FROM t_result r "
                + "JOIN t_entry e ON e.id = r.entry_id JOIN t_band b ON b.id = e.band_id "
                + "WHERE r.race_id = ? ORDER BY r.rank_no", rs -> {
            int expectedRank = rs.getInt("rank_no");
            String dbBand = rs.getString("band_code");
            BigDecimal dbSpeed = rs.getBigDecimal("speed_mpm");
            JsonNode board = boardAll.get(expectedRank - 1);
            assertEquals(dbBand, board.get("bandCode").asText());
            assertEquals(0, dbSpeed.compareTo(new BigDecimal(board.get("speedMpm").asText())));
            assertEquals(expectedRank, board.get("rankNo").asInt());
        }, raceId);
    }

    @Test
    void scoreBeforeAnyClockProducesEmptyBoard() throws Exception {
        JsonNode score = postScore();
        assertEquals(0, score.get("code").asInt());
        assertEquals(0, score.get("data").size());
        assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_result WHERE race_id = ?", Integer.class, raceId));
        JsonNode rank = getRank(1, 20);
        assertEquals(0, rank.get("data").get("total").asInt());
    }

    @Test
    void rankOnMissingRaceGivesBizMessage() {
        web.get().uri("/api/pigeon/races/9999/rank")
                .header("Authorization", BASIC)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.code").isEqualTo(1)
                .jsonPath("$.msg").value(v -> assertNotNull(v));
    }

    private void checkResultRow(JsonNode row, long entryId, String speed, int rank) {
        assertEquals(entryId, row.get("entryId").asLong());
        // 用数值比较：H2 返回的 BigDecimal 标度可能是 1（1250.0），MySQL DECIMAL(12,2) 为 1250.00
        assertEquals(0, new BigDecimal(speed).compareTo(new BigDecimal(row.get("speedMpm").asText())));
        assertEquals(rank, row.get("rankNo").asInt());
    }

    private void checkRankRow(JsonNode row, String bandCode, String owner, String speed, int rank) {
        assertEquals(bandCode, row.get("bandCode").asText());
        assertEquals(owner, row.get("ownerName").asText());
        assertEquals(0, new BigDecimal(speed).compareTo(new BigDecimal(row.get("speedMpm").asText())));
        assertEquals(rank, row.get("rankNo").asInt());
    }
}

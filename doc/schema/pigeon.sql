-- any-02-pigeon · 赛鸽训放 归巢计时与名次 · 建表 SQL
-- 字符集 utf8mb4，时区 Asia/Shanghai。create 阶段建好，模型只写业务代码，不碰建表。
-- 列名即契约：del_flag 由 @TableLogic 自动拼接（查询带 del_flag=0，删除置 1），
-- create_by/update_by/create_time/update_time 由 AutoFillMetaObjectHandler 自动填充，业务代码不要手写。
-- 主键 id 由应用侧雪花分配（IdType.INPUT），不依赖自增。

-- 1) 足环档案
CREATE TABLE IF NOT EXISTS t_band (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    band_code   VARCHAR(32)  NOT NULL COMMENT '足环号，全国唯一（如 CHN2026-A-123456）',
    band_year   INT          NOT NULL COMMENT '足环年份',
    owner_name  VARCHAR(64)  NOT NULL COMMENT '当前鸽主姓名',
    loft_city   VARCHAR(64)  DEFAULT NULL COMMENT '鸽舍所在城市',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 在赛 / SUSPENDED 停赛 / RETIRED 注销',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_band_code (band_code),
    KEY idx_owner (owner_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='足环档案';

-- 2) 足环过户登记
CREATE TABLE IF NOT EXISTS t_band_transfer (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    band_id     BIGINT       NOT NULL COMMENT '足环 id（t_band.id）',
    from_owner  VARCHAR(64)  NOT NULL COMMENT '过户前鸽主',
    to_owner    VARCHAR(64)  NOT NULL COMMENT '过户后鸽主',
    transfer_at DATETIME     NOT NULL COMMENT '过户时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    KEY idx_band (band_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='足环过户登记';

-- 3) 训放计划（一场训放/赛事）
CREATE TABLE IF NOT EXISTS t_race (
    id           BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    race_code    VARCHAR(32)  NOT NULL COMMENT '赛项编号，唯一（如 XF-2026-018）',
    title        VARCHAR(128) NOT NULL COMMENT '赛项名称',
    release_site VARCHAR(128) NOT NULL COMMENT '放飞地',
    release_at   DATETIME     NOT NULL COMMENT '放飞时刻（开笼时间）',
    close_at     DATETIME     DEFAULT NULL COMMENT '关门时刻（报到截止；空=不限）',
    distance_km  DECIMAL(8,3) NOT NULL COMMENT '空距（公里）',
    status       VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT 筹备 / SEALED 已集鸽 / RELEASED 已放飞 / CLOSED 已关棚',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by    VARCHAR(64)  DEFAULT NULL,
    create_time  DATETIME     DEFAULT NULL,
    update_by    VARCHAR(64)  DEFAULT NULL,
    update_time  DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_race_code (race_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训放计划';

-- 4) 集鸽登记（一羽足环报进一场赛）
CREATE TABLE IF NOT EXISTS t_entry (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    race_id     BIGINT       NOT NULL COMMENT '赛项 id（t_race.id）',
    band_id     BIGINT       NOT NULL COMMENT '足环 id（t_band.id）',
    basket_no   VARCHAR(16)  DEFAULT NULL COMMENT '笼筐号',
    entry_time  DATETIME     DEFAULT NULL COMMENT '集鸽时间',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_race_band (race_id, band_id),
    KEY idx_race (race_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集鸽登记';

-- 5) 归巢报到（一羽赛鸽一场赛只计第一次有效归巢）
CREATE TABLE IF NOT EXISTS t_clocking (
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    entry_id    BIGINT       NOT NULL COMMENT '集鸽登记 id（t_entry.id）',
    race_id     BIGINT       NOT NULL COMMENT '赛项 id（t_race.id）',
    clock_at    DATETIME     NOT NULL COMMENT '归巢时刻（扫描/补录）',
    source      VARCHAR(16)  NOT NULL DEFAULT 'SCAN' COMMENT 'SCAN 扫描 / MANUAL 手工补录',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_entry (entry_id),
    KEY idx_race (race_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归巢报到';

-- 6) 名次结果（分速与名次，重算覆盖不重插）
CREATE TABLE IF NOT EXISTS t_result (
    id          BIGINT        NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    race_id     BIGINT        NOT NULL COMMENT '赛项 id（t_race.id）',
    entry_id    BIGINT        NOT NULL COMMENT '集鸽登记 id（t_entry.id）',
    speed_mpm   DECIMAL(12,2) NOT NULL COMMENT '分速（米/分钟，保留 2 位）',
    rank_no     INT           DEFAULT NULL COMMENT '名次（1 起，按分速降序）',
    del_flag    TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64)   DEFAULT NULL,
    create_time DATETIME      DEFAULT NULL,
    update_by   VARCHAR(64)   DEFAULT NULL,
    update_time DATETIME      DEFAULT NULL,
    UNIQUE KEY uk_race_entry (race_id, entry_id),
    KEY idx_race (race_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='名次结果';

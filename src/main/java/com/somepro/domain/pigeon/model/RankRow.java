package com.somepro.domain.pigeon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 名次榜读模型（领域层值对象，不可变 record）：
 * 一行 = 足环号 + 鸽主 + 归巢时刻 + 分速 + 名次。
 *
 * 由基础设施层联表 t_result / t_clocking / t_entry / t_band 查出，
 * 接口层原样转成 VO 输出，保证榜上的数与库里成绩同源。
 */
public record RankRow(Long entryId,
                      Long bandId,
                      String bandCode,
                      String ownerName,
                      LocalDateTime clockAt,
                      BigDecimal speedMpm,
                      Integer rankNo) {
}

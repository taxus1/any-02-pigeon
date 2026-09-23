package com.somepro.domain.pigeon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 名次榜行（领域读模型，不可变 record）：跨 t_result / t_entry / t_band / t_clocking
 * 拼出的展示数据。分速与名次直接取自已落库的 t_result，保证榜上的数与库里的成绩一致。
 */
public record LeaderboardRow(Integer rankNo, String bandCode, String ownerName,
                             LocalDateTime clockAt, BigDecimal speedMpm) {
}

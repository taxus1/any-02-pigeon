package com.somepro.infrastructure.persistence.pigeon.po;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 名次榜联表查询的投影 PO（不映射具体某张表，无 BasePO / 审计字段）。
 *
 * 由 RaceResultMapper 的自定义联表 SQL 填充：
 * t_result 取分速/名次，t_clocking 取归巢时刻，t_entry 连 t_band 取足环号/鸽主。
 * 所有参与联表都显式带 del_flag = 0（@TableLogic 不会给手写 SQL 自动拼条件）。
 */
@Getter
@Setter
public class RankRowPO {

    private Long entryId;

    private Long bandId;

    private String bandCode;

    private String ownerName;

    private LocalDateTime clockAt;

    private BigDecimal speedMpm;

    private Integer rankNo;
}

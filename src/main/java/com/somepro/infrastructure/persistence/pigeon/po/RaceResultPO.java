package com.somepro.infrastructure.persistence.pigeon.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * t_result（名次结果）PO。
 */
@Getter
@Setter
@TableName("t_result")
public class RaceResultPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("race_id")
    private Long raceId;

    @TableField("entry_id")
    private Long entryId;

    @TableField("speed_mpm")
    private BigDecimal speedMpm;

    @TableField("rank_no")
    private Integer rankNo;
}

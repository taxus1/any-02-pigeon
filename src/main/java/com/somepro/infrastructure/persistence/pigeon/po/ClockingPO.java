package com.somepro.infrastructure.persistence.pigeon.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * t_clocking 表的持久化对象（PO，基础设施层）。只描述表的形状，不放业务规则。
 * source 在库里是 VARCHAR，枚举转换在 ClockingPoConverter 里做。
 */
@Getter
@Setter
@TableName("t_clocking")
public class ClockingPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("entry_id")
    private Long entryId;

    @TableField("race_id")
    private Long raceId;

    @TableField("clock_at")
    private LocalDateTime clockAt;

    @TableField("source")
    private String source;
}

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
 * t_clocking（归巢报到）PO。
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

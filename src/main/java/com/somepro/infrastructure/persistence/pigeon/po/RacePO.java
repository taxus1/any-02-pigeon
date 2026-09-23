package com.somepro.infrastructure.persistence.pigeon.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * t_race 表的持久化对象（PO，基础设施层）。只描述表的形状，不放业务规则。
 */
@Getter
@Setter
@TableName("t_race")
public class RacePO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("race_code")
    private String raceCode;

    @TableField("title")
    private String title;

    @TableField("release_site")
    private String releaseSite;

    @TableField("release_at")
    private LocalDateTime releaseAt;

    @TableField("close_at")
    private LocalDateTime closeAt;

    @TableField("distance_km")
    private BigDecimal distanceKm;

    @TableField("status")
    private String status;
}

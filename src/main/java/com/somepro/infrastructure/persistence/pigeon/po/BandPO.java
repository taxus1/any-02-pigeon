package com.somepro.infrastructure.persistence.pigeon.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

/**
 * t_band 表的持久化对象（PO，基础设施层）。只描述表的形状，不放业务规则。
 */
@Getter
@Setter
@TableName("t_band")
public class BandPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("band_code")
    private String bandCode;

    @TableField("band_year")
    private Integer bandYear;

    @TableField("owner_name")
    private String ownerName;

    @TableField("loft_city")
    private String loftCity;

    @TableField("status")
    private String status;
}

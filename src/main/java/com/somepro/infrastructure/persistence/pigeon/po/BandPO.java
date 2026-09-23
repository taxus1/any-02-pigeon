package com.somepro.infrastructure.persistence.pigeon.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

/**
 * t_band（足环档案）PO。只描述表结构，业务规则在领域 Band。
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

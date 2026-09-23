package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 足环档案的领域对象（纯领域，无框架注解）。
 * 后半条线只读它：报到时按足环号找到鸽子，名次榜上展示足环号与鸽主。
 */
@Getter
@Setter
public class Band extends BaseEntity {

    private Long id;

    /** 足环号，全国唯一（如 CHN2026-A-123456）。 */
    private String bandCode;

    private Integer bandYear;

    private String ownerName;

    private String loftCity;

    /** ACTIVE 在赛 / SUSPENDED 停赛 / RETIRED 注销。 */
    private String status;
}

package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 足环档案（领域层，赛鸽上下文）。
 *
 * 纯领域对象，不带任何持久化注解；本表在本用例里是「档案参照」，
 * 报到时按足环号找到鸽子与鸽主，不产生新的业务行为。
 */
@Getter
@Setter
public class Band extends BaseEntity {

    private Long id;

    /** 足环号，全国唯一（如 CHN2026-A-123456）。 */
    private String bandCode;

    private Integer bandYear;

    /** 当前鸽主姓名（名次榜展示用）。 */
    private String ownerName;

    private String loftCity;

    /** ACTIVE 在赛 / SUSPENDED 停赛 / RETIRED 注销。 */
    private String status;

    /**
     * 领域校验：只有在赛状态的足环才能参加训放。
     * 停赛 / 注销足环不接受报到。
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}

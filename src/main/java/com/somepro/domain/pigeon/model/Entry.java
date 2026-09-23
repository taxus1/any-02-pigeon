package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 集鸽登记（领域层，赛鸽上下文）：一羽足环报进一场赛。
 *
 * 是「报到」的前置：只有集过鸽的鸽子回来才允许记一笔。
 * t_entry 上有 (race_id, band_id) 唯一约束，一场赛一羽足环至多一条集鸽记录。
 */
@Getter
@Setter
public class Entry extends BaseEntity {

    private Long id;

    private Long raceId;

    private Long bandId;

    /** 笼筐号。 */
    private String basketNo;

    private LocalDateTime entryTime;
}

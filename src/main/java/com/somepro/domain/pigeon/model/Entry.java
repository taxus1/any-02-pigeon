package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 集鸽登记（一羽足环报进一场赛）的领域对象（纯领域，无框架注解）。
 * 后半条线只读它：报到前必须查得到集鸽记录，否则说明这羽鸽子根本没进本场。
 */
@Getter
@Setter
public class Entry extends BaseEntity {

    private Long id;

    private Long raceId;

    private Long bandId;

    private String basketNo;

    private LocalDateTime entryTime;
}

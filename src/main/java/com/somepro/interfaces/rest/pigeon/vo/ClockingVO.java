package com.somepro.interfaces.rest.pigeon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 归巢报到回执（接口层）：只暴露允许外部看到的字段。
 */
public record ClockingVO(Long id,
                         Long raceId,
                         Long entryId,
                         @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                         LocalDateTime clockAt,
                         String source) implements Serializable {
}

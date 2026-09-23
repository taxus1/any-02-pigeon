package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 归巢报到对外返回对象（VO，用户接口层）—— 不可变 record。
 * 只暴露对外字段，delFlag / createBy / updateBy 等内部字段不进 API 契约。
 */
public record ClockingVO(Long id, Long raceId, Long entryId, LocalDateTime clockAt, String source)
        implements Serializable {
}

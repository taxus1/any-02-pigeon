package com.somepro.interfaces.rest.pigeon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * 归巢报到入参（接口层）。
 *
 * @param clockAt 归巢时刻，按 yyyy-MM-dd HH:mm:ss 传（GMT+8）
 * @param source  SCAN 扫描 / MANUAL 手工补录
 */
public record ClockInRequest(
        @NotNull(message = "赛项 id 不能为空")
        Long raceId,

        @NotBlank(message = "足环号不能为空")
        String bandCode,

        @NotNull(message = "归巢时刻不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime clockAt,

        @NotBlank(message = "报到来源不能为空")
        @Pattern(regexp = "(?i)SCAN|MANUAL", message = "报到来源只支持 SCAN（扫描）/ MANUAL（手工补录）")
        String source) {
}

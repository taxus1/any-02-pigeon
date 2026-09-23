package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;

/**
 * 报到来源：SCAN 扫描 / MANUAL 手工补录。
 * 落库时存枚举名（t_clocking.source 为 VARCHAR）。
 */
public enum ClockingSource {

    SCAN,
    MANUAL;

    /** 解析外部输入；不认识的来源给出明确说法，不静默放行。 */
    public static ClockingSource from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BizException("报到来源不能为空（SCAN 扫描 / MANUAL 手工补录）");
        }
        try {
            return ClockingSource.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("报到来源只支持 SCAN / MANUAL，收到：" + raw);
        }
    }
}

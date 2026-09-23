package com.somepro.domain.pigeon.model;

/**
 * 归巢报到来源。
 * SCAN：扫描器自动扫描；MANUAL：人工补录。
 * 落库存字符串（t_clocking.source），不引入框架枚举映射。
 */
public enum ClockSource {

    SCAN,
    MANUAL;

    /** 解析入参；非法来源给明确说法，不闷头入库。 */
    public static ClockSource of(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        for (ClockSource s : values()) {
            if (s.name().equalsIgnoreCase(trimmed)) {
                return s;
            }
        }
        return null;
    }
}

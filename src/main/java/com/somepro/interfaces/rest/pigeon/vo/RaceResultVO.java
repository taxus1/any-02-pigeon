package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出成绩结果的单行（接口层）：分速与名次，按名次顺序返回。
 */
public record RaceResultVO(Long entryId,
                           BigDecimal speedMpm,
                           Integer rankNo) implements Serializable {
}

package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;

/**
 * 成绩重算结果对外返回对象（VO，用户接口层）—— 不可变 record。
 * resultCount 为本次重算落库的成绩条数（即本场有效归巢数）。
 */
public record RecalcSummaryVO(Long raceId, Integer resultCount) implements Serializable {
}

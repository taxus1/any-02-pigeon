package com.somepro.interfaces.rest.pigeon.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 名次榜行对外返回对象（VO，用户接口层）—— 不可变 record。
 * 每行：名次、足环号、鸽主、归巢时刻、分速（米/分钟，两位小数）。
 */
public record LeaderboardRowVO(Integer rankNo, String bandCode, String ownerName,
                               LocalDateTime clockAt, BigDecimal speedMpm) implements Serializable {
}

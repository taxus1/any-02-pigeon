package com.somepro.interfaces.rest.pigeon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 名次榜单行（接口层）：足环号、鸽主、归巢时刻、分速、名次。
 * 数据来自 t_result 联表查询，与库里成绩同源。
 */
public record RankRowVO(String bandCode,
                        String ownerName,
                        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                        LocalDateTime clockAt,
                        BigDecimal speedMpm,
                        Integer rankNo) implements Serializable {
}

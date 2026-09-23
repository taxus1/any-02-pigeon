package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 归巢报到 Mapper。
 */
@Mapper
public interface ClockingMapper extends BaseMapper<ClockingPO> {
}

package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.RacePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训放赛项 Mapper。
 */
@Mapper
public interface RaceMapper extends BaseMapper<RacePO> {
}

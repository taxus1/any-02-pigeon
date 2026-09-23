package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * t_clocking 的 MyBatis-Plus Mapper（基础设施层）。无自定义 SQL。
 * 阻塞 JDBC API，只能在仓储适配器的 blocking(...) 里调用。
 */
@Mapper
public interface ClockingMapper extends BaseMapper<ClockingPO> {
}

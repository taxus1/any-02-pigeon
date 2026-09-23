package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * t_entry 的 MyBatis-Plus Mapper（基础设施层）。只读，无自定义 SQL。
 * 阻塞 JDBC API，只能在仓储适配器的 blocking(...) 里调用。
 */
@Mapper
public interface EntryMapper extends BaseMapper<EntryPO> {
}

package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 集鸽登记 Mapper。
 */
@Mapper
public interface EntryMapper extends BaseMapper<EntryPO> {
}

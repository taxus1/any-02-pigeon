package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 足环档案 Mapper。阻塞 JDBC，只允许在仓储适配器的 blocking(...) 里调用。
 */
@Mapper
public interface BandMapper extends BaseMapper<BandPO> {
}

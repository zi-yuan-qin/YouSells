package com.yousells.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yousells.modules.customer.entity.AiInsightCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiInsightCacheMapper extends BaseMapper<AiInsightCache> {

    AiInsightCache selectValidByCustomerId(@Param("customerId") Long customerId);

    void deleteByCustomerId(@Param("customerId") Long customerId);
}

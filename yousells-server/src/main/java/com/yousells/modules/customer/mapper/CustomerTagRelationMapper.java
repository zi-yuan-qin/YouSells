package com.yousells.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yousells.modules.customer.entity.CustomerTagRelationEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerTagRelationMapper extends BaseMapper<CustomerTagRelationEntity> {

    List<String> selectTagNamesByCustomerId(@Param("customerId") Long customerId);

    List<CustomerTagMapping> selectTagNamesByCustomerIds(@Param("customerIds") List<Long> customerIds);

    @Data
    class CustomerTagMapping {
        private Long customerId;
        private String tagName;
    }
}

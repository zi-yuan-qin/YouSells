package com.yousells.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.customer.entity.CustomerEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {

    IPage<CustomerEntity> pageCustomers(Page<?> page,
                                        @Param("keyword") String keyword,
                                        @Param("intentLevel") String intentLevel,
                                        @Param("currentStage") String currentStage,
                                        @Param("sourcePlatform") String sourcePlatform,
                                        @Param("ownerUserId") Long ownerUserId);

    List<OwnerDisplayName> selectOwnerDisplayNames(@Param("userIds") List<Long> userIds);

    @Data
    class OwnerDisplayName {
        private Long userId;
        private String displayName;
    }
}

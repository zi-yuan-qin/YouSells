package com.yousells.modules.followup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yousells.modules.followup.entity.FollowUpEntity;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUpEntity> {

    IPage<FollowUpEntity> pageFollowUps(Page<?> page, @Param("customerId") Long customerId);

    List<UserDisplayName> selectUserDisplayNames(@Param("userIds") List<Long> userIds);

    @Data
    class UserDisplayName {
        private Long userId;
        private String displayName;
    }
}

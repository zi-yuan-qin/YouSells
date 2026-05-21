package com.yousells.common.security;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yousells.modules.auth.entity.UserEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DataScopeHelper {

    private DataScopeHelper() {
    }

    /**
     * 递归查询指定用户的所有下级 ID（含间接下级）。
     * @param userId  当前用户 ID
     * @param userMapper  用户 Mapper
     * @return 下级用户 ID 列表（含直接下级和间接下级）
     */
    public static List<Long> getSubordinateIds(Long userId, BaseMapper<UserEntity> userMapper) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> directSubordinates = queryDirectSubordinates(userId, userMapper);
        List<Long> allSubordinates = new ArrayList<>(directSubordinates);
        for (Long subId : directSubordinates) {
            allSubordinates.addAll(getSubordinateIds(subId, userMapper));
        }
        return allSubordinates;
    }

    private static List<Long> queryDirectSubordinates(Long managerId, BaseMapper<UserEntity> userMapper) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getManagerUserId, managerId)
                .eq(UserEntity::getIsDeleted, 0);
        return userMapper.selectList(wrapper).stream()
                .map(UserEntity::getId)
                .toList();
    }
}

package com.yousells.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.modules.auth.dto.CreateUserRequest;
import com.yousells.modules.auth.dto.ResignUserRequest;
import com.yousells.modules.auth.dto.UpdatePasswordRequest;
import com.yousells.modules.auth.dto.UpdateProfileRequest;
import com.yousells.modules.auth.dto.UpdateUserRequest;
import com.yousells.modules.auth.entity.UserEntity;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.auth.service.UserService;
import com.yousells.modules.auth.vo.UserListItemVo;
import com.yousells.common.security.DataScopeHelper;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.vo.UserProfileVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserListItemVo> listUsers() {
        List<UserEntity> entities = userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getStatus, "ACTIVE")
                        .orderByAsc(UserEntity::getId)
        );
        return entities.stream()
                .map(e -> new UserListItemVo(
                        e.getId(),
                        e.getUsername(),
                        e.getDisplayName(),
                        e.getLevel(),
                        e.getManagerUserId(),
                        e.getStatus()
                ))
                .toList();
    }

    @Override
    public UserProfileVo getUser(Long userId) {
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        if (!currentUser.userId().equals(userId)) {
            List<Long> subordinateIds = DataScopeHelper.getSubordinateIds(currentUser.userId(), userMapper);
            if (!subordinateIds.contains(userId)) {
                throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权查看该用户");
            }
        }
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "user not found");
        }
        return toProfileVo(entity);
    }

    @Override
    public UserProfileVo updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "user not found");
        }
        entity.setRealName(request.realName());
        entity.setDisplayName(request.realName());
        int rows = userMapper.updateById(entity);
        if (rows == 0) {
            throw new BusinessException(ErrorCodeConstants.INTERNAL_ERROR, "update failed");
        }
        return toProfileVo(userMapper.selectById(userId));
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "user not found");
        }
        if (!passwordEncoder.matches(request.oldPassword(), entity.getPasswordHash())) {
            throw new BusinessException(ErrorCodeConstants.BAD_REQUEST, "old password incorrect");
        }
        entity.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(entity);
        DataScopeHelper.invalidateAllCache();
    }

    @Override
    @Transactional
    public Long createUser(CreateUserRequest request) {
        requireT3OrAdmin();
        UserEntity existing = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, request.username())
        );
        if (existing != null) {
            throw new BusinessException(ErrorCodeConstants.STATUS_CONFLICT, "username already exists");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(request.username());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRealName(request.realName());
        entity.setDisplayName(request.realName());
        entity.setLevel(request.level());
        entity.setManagerUserId(request.managerUserId());
        entity.setStatus("ACTIVE");
        userMapper.insert(entity);
        DataScopeHelper.invalidateAllCache();
        return entity.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long userId, UpdateUserRequest request) {
        requireT3OrAdmin();
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "user not found");
        }
        entity.setRealName(request.realName());
        entity.setDisplayName(request.realName());
        entity.setLevel(request.level());
        entity.setManagerUserId(request.managerUserId());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        userMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void resignUser(Long userId, ResignUserRequest request) {
        requireT3OrAdmin();
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException(ErrorCodeConstants.NOT_FOUND, "user not found");
        }
        // 检查是否有下属
        Long subordinateCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getManagerUserId, userId)
                        .eq(UserEntity::getStatus, "ACTIVE")
        );
        if (subordinateCount > 0) {
            throw new BusinessException(ErrorCodeConstants.BAD_REQUEST,
                    "该成员还有 " + subordinateCount + " 名下属，请先转移下属后再办理离职");
        }
        entity.setStatus("DISABLED");
        entity.setResignReason(request.reason());
        userMapper.updateById(entity);
        // 软删除
        userMapper.deleteById(userId);
        DataScopeHelper.invalidateAllCache();
    }

    private void requireT3OrAdmin() {
        LoginUser currentUser = SecurityUserContext.requireCurrentUser();
        if (!"T3".equals(currentUser.level())) {
            throw new BusinessException(ErrorCodeConstants.FORBIDDEN, "无权操作");
        }
    }

    private UserProfileVo toProfileVo(UserEntity entity) {
        return new UserProfileVo(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getLevel(),
                entity.getManagerUserId(),
                entity.getStatus()
        );
    }
}

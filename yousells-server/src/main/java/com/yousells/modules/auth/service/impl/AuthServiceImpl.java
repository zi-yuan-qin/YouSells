package com.yousells.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yousells.common.config.JwtProperties;
import com.yousells.common.constant.ErrorCodeConstants;
import com.yousells.common.exception.BusinessException;
import com.yousells.common.security.JwtTokenProvider;
import com.yousells.common.security.LoginUser;
import com.yousells.common.security.SecurityUserContext;
import com.yousells.modules.auth.dto.LoginRequest;
import com.yousells.modules.auth.entity.UserEntity;
import com.yousells.modules.auth.mapper.UserMapper;
import com.yousells.modules.auth.service.AuthService;
import com.yousells.modules.auth.vo.CurrentUserVo;
import com.yousells.modules.auth.vo.LoginResultVo;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void seedUsers() {
        seedUserIfNotExists(1L, "admin", "秦梓源", "T2", null, "admin123");
        seedUserIfNotExists(2L, "member", "小赵", "T0", 1L, "member123");
    }

    private void seedUserIfNotExists(Long id, String username, String realName, String level, Long managerUserId, String rawPassword) {
        if (userMapper.selectById(id) != null) {
            return;
        }
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setLevel(level);
        user.setManagerUserId(managerUserId);
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }

    @Override
    public LoginResultVo login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCodeConstants.UNAUTHORIZED, "invalid username or password");
        }
        LoginUser loginUser = new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getLevel(),
                user.getManagerUserId()
        );
        return new LoginResultVo(
                jwtTokenProvider.createAccessToken(loginUser),
                TOKEN_TYPE,
                jwtProperties.accessTokenExpireSeconds(),
                toVo(loginUser)
        );
    }

    @Override
    public CurrentUserVo currentUser() {
        return toVo(SecurityUserContext.requireCurrentUser());
    }

    @Override
    public void logout() {
    }

    private CurrentUserVo toVo(LoginUser loginUser) {
        return new CurrentUserVo(
                loginUser.userId(),
                loginUser.username(),
                loginUser.realName(),
                loginUser.level(),
                loginUser.managerUserId()
        );
    }
}

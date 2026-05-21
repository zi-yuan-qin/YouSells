package com.yousells.common.security;

import com.yousells.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.accessTokenExpireSeconds());
        return Jwts.builder()
                .subject(String.valueOf(loginUser.userId()))
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .claim("username", loginUser.username())
                .claim("realName", loginUser.realName())
                .claim("level", loginUser.level())
                .claim("managerUserId", loginUser.managerUserId())
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseLoginUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String realName = claims.get("realName", String.class);
        String level = claims.get("level", String.class);
        Long managerUserId = claims.get("managerUserId", Long.class);
        return new LoginUser(userId, username, realName, level, managerUserId);
    }
}

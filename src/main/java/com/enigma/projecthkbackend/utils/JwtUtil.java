package com.enigma.projecthkbackend.utils;

import com.enigma.projecthkbackend.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Getter
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String SECRET;
    @Value("${app.jwt.expiry}")
    private Long ACCESS_EXPIRY;
    @Value("${app.jwt.refresh-expiry}")
    private Long REFRESH_EXPIRY;

    private SecretKey getKey()
    {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user)
    {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRY*1000))
                .signWith(getKey())
                .compact();
    }

    public String generetRefreshToken(User user)
    {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRY * 1000))
                .signWith(getKey())
                .compact();
    }

    public Claims validateAndExtract(String token)
    {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isRefreshToken(Claims claims)
    {
        return "refresh".equals(claims.get("type"));
    }

}

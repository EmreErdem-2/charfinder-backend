package com.example.charfinder.auth;

import com.example.charfinder.user.Role;
import com.example.charfinder.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {
    @Value("${security.jwt.secret}") private String secret;          // HS256
    @Value("${security.jwt.accessTtlSeconds}") private long accessTtl;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtl)))
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Role::getName).toList())
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String jwt) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(jwt).getBody();
    }
}

package com.example.charfinder.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {
    @Value("${security.jwt.refreshTtlSeconds}") private long refreshTtl;

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true) // false in local dev if not HTTPS
                .path("/")
                .maxAge(Duration.ofSeconds(refreshTtl))
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}

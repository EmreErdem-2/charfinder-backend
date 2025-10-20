package com.example.charfinder.auth.services;

import com.example.charfinder.auth.RefreshTokenRepository;
import com.example.charfinder.auth.tables.RefreshToken;
import com.example.charfinder.auth.tables.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final PasswordEncoder encoder; // use a separate hasher if you prefer
    @Value("${security.jwt.refreshTtlSeconds}") private long refreshTtl;

    public RefreshToken issue(User user, String rawToken) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(encoder.encode(rawToken)); // hash before storing
        rt.setIssuedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plusSeconds(refreshTtl));
        rt.setRevoked(false);
        return repo.save(rt);
    }

    public Optional<RefreshToken> validateAndRotate(User user, String rawToken) {
        // find all active tokens for user, verify one matches and not expired/revoked
        List<RefreshToken> tokens = repo.findByUserIdAndRevokedFalseAndExpiresAtAfter(user.getId(), Instant.now());
        RefreshToken current = tokens.stream()
                .filter(t -> encoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElse(null);
        if (current == null) return Optional.empty();

        // revoke current and issue a new one
        current.setRevoked(true);
        RefreshToken next = issue(user, generateSecureRandomToken()); // 256-bit random
        current.setReplacedBy(next);
        repo.save(current);

        return Optional.of(next);
    }

    public void revokeAll(User user) {
        repo.revokeAllActiveByUserId(user.getId(), Instant.now());
    }

    public String generateSecureRandomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

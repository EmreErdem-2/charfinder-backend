package com.example.charfinder.token;

import com.example.charfinder.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

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

    public RefreshRotationResult validateAndRotate(User user, String rawToken) {
        // find active tokens
        List<RefreshToken> tokens = repo.findByUserIdAndRevokedFalseAndExpiresAtAfter(
                user.getId(), Instant.now());

        RefreshToken current = tokens.stream()
                .filter(t -> encoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // revoke current
        current.setRevoked(true);

        // issue new
        String newRaw = generateSecureRandomToken();
        RefreshToken next = new RefreshToken();
        next.setUser(user);
        next.setTokenHash(encoder.encode(newRaw));
        next.setIssuedAt(Instant.now());
        next.setExpiresAt(Instant.now().plusSeconds(refreshTtl));
        next.setRevoked(false);

        repo.save(next);
        current.setReplacedBy(next);
        repo.save(current);

        return new RefreshRotationResult(next, newRaw);
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

package com.example.charfinder.auth;

import com.example.charfinder.auth.tables.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("select t from RefreshToken t where t.user.id = :userId and t.revoked = false and t.expiresAt > :now")
    List<RefreshToken> findByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.revoked = false and t.expiresAt > :now")
    void revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
package com.example.charfinder.token;

import com.example.charfinder.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens",
        indexes = { @Index(name = "idx_refresh_user", columnList = "user_id") })
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private String tokenHash;         // store hashed
    @Column(nullable = false) private Instant issuedAt;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked = false;
    @OneToOne @JoinColumn(name = "replaced_by_token_id") private RefreshToken replacedBy;
}

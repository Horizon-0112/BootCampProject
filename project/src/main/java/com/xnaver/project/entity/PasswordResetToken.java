package com.xnaver.project.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Entity
@Table(name = "password_reset_tokens")
@Getter @NoArgsConstructor
public class PasswordResetToken {
    @Id @Column(length = 64)
    private String tokenHash;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true, columnDefinition = "int")
    private UserEntity user;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private Instant issuedAt;
    public PasswordResetToken(String hash, UserEntity user, Instant now) {
        this.tokenHash = hash;
        this.user = user;
        this.issuedAt = now;
        this.expiresAt = now.plusSeconds(1800);
    }
}

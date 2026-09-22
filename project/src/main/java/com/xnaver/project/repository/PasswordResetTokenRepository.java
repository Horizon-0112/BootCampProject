package com.xnaver.project.repository;
import com.xnaver.project.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findByUserIdx(Long userId);
}

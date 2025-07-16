package kr.co.amateurs.server.repository.auth;

import kr.co.amateurs.server.domain.entity.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * 토큰으로 비밀번호 재설정 토큰 찾기
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 이메일로 비밀번호 재설정 토큰 찾기 (가장 최근 것)
     */
    Optional<PasswordResetToken> findFirstByEmailOrderByCreatedAtDesc(String email);

    /**
     * 이메일로 기존 토큰들 삭제 (새 토큰 생성 전)
     */
    void deleteByEmail(String email);

    /**
     * 만료된 토큰들 삭제 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);

    /**
     * 이메일로 유효한 토큰 존재 여부 확인
     */
    @Query("SELECT COUNT(p) > 0 FROM PasswordResetToken p WHERE p.email = :email AND p.expiresAt > :now")
    boolean existsValidTokenByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}
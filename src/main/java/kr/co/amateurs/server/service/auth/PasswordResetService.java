package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmResponseDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetResponseDTO;
import kr.co.amateurs.server.domain.entity.auth.PasswordResetToken;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.auth.PasswordResetTokenRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public PasswordResetResponseDTO requestPasswordReset(PasswordResetRequestDTO request) {
        User user = userService.findByEmail(request.email());

        if (user.isDeleted()) {
            throw ErrorCode.USER_NOT_FOUND.get();
        }
        
        if (!user.getName().equals(request.name().trim())) {
            throw ErrorCode.INVALID_USER_INFO.get();
        }

        if (!user.getNickname().equals(request.nickname().trim())) {
            throw ErrorCode.INVALID_USER_INFO.get();
        }

        // 3. 기존 토큰 삭제 (동일 이메일의 기존 요청 무효화)
        passwordResetTokenRepository.deleteByEmail(request.email());

        // 4. 새 토큰 생성
        String token = generateResetToken();

        // 5. 토큰 저장 (MySQL에 30분 만료시간으로 저장)
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(request.email())
                .build();

        passwordResetTokenRepository.save(resetToken);

        // 6. 보안 로그 (민감정보 제외)
        log.info("비밀번호 재설정 토큰 생성 - 이메일: {}, 성공",
                maskEmail(request.email()));

        // 7. 이메일 전송
        emailService.sendPasswordResetEmail(request.email(), token);

        return PasswordResetResponseDTO.success(token);
    }

    /**
     * 비밀번호 재설정 실행
     */
    @Transactional
    public PasswordResetConfirmResponseDTO confirmPasswordReset(PasswordResetConfirmDTO request) {
        // 1. 비밀번호 확인 검증
        if (!request.isPasswordMatch()) {
            throw ErrorCode.PASSWORD_MISMATCH.get();
        }

        // 2. 토큰 검증
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> ErrorCode.INVALID_RESET_TOKEN.get());

        // 3. 토큰 만료 확인
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw ErrorCode.EXPIRED_RESET_TOKEN.get();
        }

        // 4. 사용자 찾기
        User user = userService.findByEmail(resetToken.getEmail());

        // 5. 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedPassword);
        userService.saveUser(user);

        // 6. 사용된 토큰 삭제
        passwordResetTokenRepository.delete(resetToken);

        log.info("비밀번호 재설정 완료 - 이메일: {}", maskEmail(resetToken.getEmail()));

        return PasswordResetConfirmResponseDTO.success();
    }

    /**
     * 만료된 토큰들을 정기적으로 삭제 (매일 자정 실행)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.deleteExpiredTokens(now);
        log.info("만료된 비밀번호 재설정 토큰들을 정리했습니다. 시간: {}", now);
    }

    /**
     * 안전한 랜덤 토큰 생성
     */
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 이메일 마스킹 (보안 로그용)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***@" + domain;
        } else {
            return localPart.substring(0, 3) + "***@" + domain;
        }
    }
}
}
package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmResponseDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetResponseDTO;
import kr.co.amateurs.server.domain.entity.auth.PasswordResetToken;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
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

        if (user.getProviderType() != ProviderType.LOCAL) {
            switch (user.getProviderType()) {
                case GITHUB -> throw ErrorCode.GITHUB_LOGIN_PASSWORD_RESET_NOT_ALLOWED.get();
                case KAKAO -> throw ErrorCode.KAKAO_LOGIN_PASSWORD_RESET_NOT_ALLOWED.get();
                default -> throw ErrorCode.SOCIAL_LOGIN_PASSWORD_RESET_NOT_ALLOWED.get();
            }
        }

        passwordResetTokenRepository.deleteByEmail(request.email());

        String token = generateResetToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(request.email())
                .build();

        passwordResetTokenRepository.save(resetToken);

        log.info("비밀번호 재설정 토큰 생성 - 이메일: {}, 성공",
                maskEmail(request.email()));
        emailService.sendPasswordResetEmail(request.email(), token);

        return PasswordResetResponseDTO.success(token);
    }

    @Transactional
    public PasswordResetConfirmResponseDTO confirmPasswordReset(PasswordResetConfirmDTO request) {
        if (!request.isPasswordMatch()) {
            throw ErrorCode.PASSWORD_MISMATCH.get();
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> ErrorCode.INVALID_RESET_TOKEN.get());

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw ErrorCode.EXPIRED_RESET_TOKEN.get();
        }

        User user = userService.findByEmail(resetToken.getEmail());

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedPassword);
        userService.saveUser(user);

        passwordResetTokenRepository.delete(resetToken);

        log.info("비밀번호 재설정 완료 - 이메일: {}", maskEmail(resetToken.getEmail()));

        return PasswordResetConfirmResponseDTO.success();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.deleteExpiredTokens(now);
        log.info("만료된 비밀번호 재설정 토큰들을 정리했습니다. 시간: {}", now);
    }

    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

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

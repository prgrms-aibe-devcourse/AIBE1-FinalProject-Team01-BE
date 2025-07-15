package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetConfirmResponseDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetRequestDTO;
import kr.co.amateurs.server.domain.dto.auth.PasswordResetResponseDTO;
import kr.co.amateurs.server.domain.entity.auth.PasswordResetToken;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.repository.auth.PasswordResetTokenRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public class PasswordResetServiceTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void 유효한_로컬_사용자_이메일로_비밀번호_재설정_요청하면_성공한다() {
        // Given
        User user = createAndSaveLocalUser("user@test.com", "nickname", "password123");
        PasswordResetRequestDTO request = new PasswordResetRequestDTO("user@test.com");

        // When
        PasswordResetResponseDTO response = passwordResetService.requestPasswordReset(request);

        // Then
        assertThat(response.message()).isEqualTo("비밀번호 재설정 정보가 확인되었습니다. 새로운 비밀번호를 설정해주세요.");
        assertThat(response.resetToken()).isNotNull();
        assertThat(response.resetToken()).isNotBlank();
    }

    @Test
    void 존재하지_않는_사용자_이메일로_요청하면_실패한다() {
        // Given
        User user = createAndSaveLocalUser("deleted@test.com", "nickname", "password123");

        user.anonymizeAndDelete(
                "anonymous" + System.currentTimeMillis() + "@deleted.com",
                "탈퇴한사용자" + System.currentTimeMillis()
        );
        userRepository.save(user);

        PasswordResetRequestDTO request = new PasswordResetRequestDTO("deleted@test.com");

        // When & Then
        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void GitHub_소셜_로그인_사용자가_요청하면_실패한다() {
        // Given
        User user = createAndSaveGitHubUser("github@test.com", "githubNick");
        PasswordResetRequestDTO request = new PasswordResetRequestDTO("github@test.com");

        // When & Then
        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GITHUB_LOGIN_PASSWORD_RESET_NOT_ALLOWED.getMessage());
    }

    @Test
    void 비밀번호가_일치하지_않으면_실패한다() {
        // Given
        createAndSaveLocalUser("user@test.com", "nickname", "oldPassword123");
        createAndSavePasswordResetToken("valid-token", "user@test.com");

        PasswordResetConfirmDTO request = new PasswordResetConfirmDTO(
                "valid-token",
                "newPassword123",
                "differentPassword123"
        );

        // When & Then
        assertThatThrownBy(() -> passwordResetService.confirmPasswordReset(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PASSWORD_MISMATCH.getMessage());
    }

    @Test
    void 유효하지_않은_토큰으로_요청하면_실패한다() {
        // Given
        createAndSaveLocalUser("user@test.com", "nickname", "oldPassword123");

        PasswordResetConfirmDTO request = new PasswordResetConfirmDTO(
                "invalid-token",
                "newPassword123",
                "newPassword123"
        );

        // When & Then
        assertThatThrownBy(() -> passwordResetService.confirmPasswordReset(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_RESET_TOKEN.getMessage());
    }

    @Test
    void 만료된_토큰으로_요청하면_실패하고_토큰이_삭제된다() {
        // Given
        createAndSaveLocalUser("user@test.com", "nickname", "oldPassword123");
        PasswordResetToken expiredToken = createAndSaveExpiredPasswordResetToken("expired-token", "user@test.com");

        PasswordResetConfirmDTO request = new PasswordResetConfirmDTO(
                "expired-token",
                "newPassword123",
                "newPassword123"
        );

        // When & Then
        assertThatThrownBy(() -> passwordResetService.confirmPasswordReset(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EXPIRED_RESET_TOKEN.getMessage());

        assertThat(passwordResetTokenRepository.findByToken("expired-token")).isEmpty();
    }

    @Test
    void 유효한_토큰과_일치하는_비밀번호로_재설정하면_성공한다() {
        // Given
        User user = createAndSaveLocalUser("user@test.com", "nickname", "oldPassword123");
        String originalPassword = user.getPassword();

        PasswordResetToken token = createAndSavePasswordResetToken("valid-token", "user@test.com");
        PasswordResetConfirmDTO request = new PasswordResetConfirmDTO(
                "valid-token",
                "newPassword123",
                "newPassword123"
        );

        // When
        PasswordResetConfirmResponseDTO response = passwordResetService.confirmPasswordReset(request);

        // Then
        assertThat(response.message()).isEqualTo("비밀번호가 성공적으로 변경되었습니다");

        User updatedUser = userRepository.findByEmail("user@test.com").orElseThrow();
        assertThat(updatedUser.getPassword()).isNotEqualTo(originalPassword);
        assertThat(passwordEncoder.matches("newPassword123", updatedUser.getPassword())).isTrue();
        assertThat(passwordResetTokenRepository.findByToken("valid-token")).isEmpty();
    }






    // 헬퍼 메서드들
    private User createAndSaveLocalUser(String email, String nickname, String password) {
        User user = UserTestFixture.createLocalUser(email, nickname, passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    private User createAndSaveGitHubUser(String email, String nickname) {
        User user = UserTestFixture.createOAuthUser(email, nickname, ProviderType.GITHUB, "github123");
        return userRepository.save(user);
    }

    private PasswordResetToken createAndSavePasswordResetToken(String token, String email) {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .build();
        return passwordResetTokenRepository.save(resetToken);
    }

    private PasswordResetToken createAndSaveExpiredPasswordResetToken(String token, String email) {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .build();

        try {
            java.lang.reflect.Field expiresAtField = PasswordResetToken.class.getDeclaredField("expiresAt");
            expiresAtField.setAccessible(true);
            expiresAtField.set(resetToken, LocalDateTime.now().minusHours(1));
        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 설정 실패", e);
        }

        return passwordResetTokenRepository.save(resetToken);
    }
}

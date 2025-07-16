package kr.co.amateurs.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import kr.co.amateurs.server.service.auth.EmailService;

/**
 * 테스트 환경에서 사용하는 EmailService
 * 실제 메일을 전송하지 않고 로그만 출력합니다.
 */
@Service
@Profile("test")
@Primary
@Slf4j
public class TestEmailService extends EmailService {

    public TestEmailService() {
        super(null);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        // 테스트에서는 실제로 메일을 보내지 않고 로그만 출력
        log.info("🧪 TEST: 비밀번호 재설정 이메일 전송 시뮬레이션");
        log.info("   - 수신자: {}", maskEmail(to));
        log.info("   - 토큰: {}", resetToken);
        log.info("   - 상태: 전송 완료 (시뮬레이션)");
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
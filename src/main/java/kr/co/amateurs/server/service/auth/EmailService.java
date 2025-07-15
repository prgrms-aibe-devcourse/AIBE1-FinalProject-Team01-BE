package kr.co.amateurs.server.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${oauth.success-redirect-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Amateurs] 비밀번호 재설정 요청");
            helper.setText(buildPasswordResetEmailContent(resetUrl), true);

            mailSender.send(message);

            log.info("비밀번호 재설정 이메일 전송 완료: {}", maskEmail(to));

        } catch (MessagingException e) {
            log.error("비밀번호 재설정 이메일 전송 실패: {}, 오류: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildPasswordResetEmailContent(String resetUrl) {
        return String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; padding: 30px; background-color: #f8f9ff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2d4053; text-align: center; margin-bottom: 30px;">🏆 Amateurs</h2>
                
                <h3 style="color: #2d4053;">비밀번호 재설정 요청</h3>
                
                <p>안녕하세요! 비밀번호 재설정을 요청하셨습니다.</p>
                <p>아래 버튼을 클릭하여 새로운 비밀번호를 설정해 주세요.</p>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="display: inline-block; background: #007bff; color: white; padding: 15px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; box-shadow: 0 2px 5px rgba(0,123,255,0.3);">
                        🔒 비밀번호 재설정하러 가기
                    </a>
                </div>
                
                <div style="background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin-top: 20px;">
                    <strong style="color: #856404;">⚠️ 주의사항:</strong><br>
                    <span style="color: #856404;">
                    • 이 링크는 30분 후에 만료됩니다.<br>
                    • 본인이 요청하지 않았다면 이 메일을 무시해 주세요.<br>
                    • 보안을 위해 링크를 다른 사람과 공유하지 마세요.
                    </span>
                </div>
                
                <p style="text-align: center; margin-top: 30px; color: #666; border-top: 1px solid #ddd; padding-top: 20px;">
                    감사합니다.<br><strong>Amateurs 팀</strong>
                </p>
            </div>
        </body>
        </html>
        """, resetUrl);
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
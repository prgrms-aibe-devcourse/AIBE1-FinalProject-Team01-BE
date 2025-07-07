package kr.co.amateurs.server.domain.dto.verfication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDTO {
    private boolean isValid;
    private String extractedText;
    private String message;
    private String detailMessage;
}

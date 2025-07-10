package kr.co.amateurs.server.domain.dto.verify;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyRequestDTO {
    private MultipartFile image;
} 
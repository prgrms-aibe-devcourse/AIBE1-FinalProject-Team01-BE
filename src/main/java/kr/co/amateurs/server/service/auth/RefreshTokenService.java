package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import kr.co.amateurs.server.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String email, String token, Long expiration) {
        validateInput(email, token, expiration);

        String hashedToken = hashToken(token);

        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .token(hashedToken)
                .expiration(expiration)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByEmail(String email) {
        validateEmail(email);
        return refreshTokenRepository.findById(email);
    }

    public void deleteByEmail(String email) {
        validateEmail(email);
        refreshTokenRepository.deleteById(email);
    }

    public boolean existsByEmail(String email) {
        validateEmail(email);
        return refreshTokenRepository.existsById(email);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw ErrorCode.HASH_ALGORITHM_NOT_FOUND.get();
        }
    }

    private void validateInput(String email, String token, Long expiration) {
        if (email == null || email.trim().isEmpty()) {
            throw ErrorCode.EMPTY_EMAIL.get();
        }
        if (token == null || token.trim().isEmpty()) {
            throw ErrorCode.EMPTY_TOKEN.get();
        }
        if (expiration == null || expiration <= 0) {
            throw ErrorCode.INVALID_EXPIRATION_TIME.get();
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw ErrorCode.EMPTY_EMAIL.get();
        }
    }
}
package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import kr.co.amateurs.server.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String email, String token, Long expiration) {
        validateInput(email, token, expiration);

        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .token(token)
                .expiration(expiration)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByEmail(String email) {
        validateEmail(email);
        return refreshTokenRepository.findByEmail(email);
    }

    public void deleteByEmail(String email) {
        validateEmail(email);
        refreshTokenRepository.deleteById(email);
    }

    public boolean existsByEmail(String email) {
        validateEmail(email);
        return refreshTokenRepository.existsById(email);
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
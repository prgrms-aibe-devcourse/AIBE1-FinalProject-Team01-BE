package kr.co.amateurs.server.service.auth;

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
        refreshTokenRepository.deleteByEmail(email);

        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .token(token)
                .expiration(expiration)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByEmail(String email) {
        return refreshTokenRepository.findByEmail(email);
    }

    public void deleteByEmail(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return refreshTokenRepository.existsByEmail(email);
    }
}

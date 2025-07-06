package kr.co.amateurs.server.domain.dto.auth;

import lombok.Builder;

@Builder
public record TokenInfo (
        String accessToken,
        Long accessTokenExpiresIn,
        String refreshToken,
        Long refreshTokenExpiresIn
) {
    public static TokenInfo of (String accessToken, Long accessTokenExpiresIn,
                                String refreshToken, Long refreshTokenExpiresIn) {
        return TokenInfo.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .build();
    }
}

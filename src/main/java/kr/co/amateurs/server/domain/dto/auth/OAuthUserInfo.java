package kr.co.amateurs.server.domain.dto.auth;

import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import lombok.Builder;

@Builder
public record OAuthUserInfo(
        ProviderType providerType,
        String providerId,
        String email,
        String nickname,
        String name,
        String imageUrl
) {
}
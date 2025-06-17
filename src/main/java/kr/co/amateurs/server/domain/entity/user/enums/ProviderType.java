package kr.co.amateurs.server.domain.entity.user.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType {
    LOCAL("local"),
    GOOGLE("google"),
    KAKAO("kakao"),
    GITHUB("github");

    private final String providerName;
}

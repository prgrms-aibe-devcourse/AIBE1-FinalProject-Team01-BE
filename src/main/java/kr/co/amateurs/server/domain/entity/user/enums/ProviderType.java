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

    public static ProviderType fromProviderName(String providerName) {
        for (ProviderType type : values()) {
            if (type.providerName.equalsIgnoreCase(providerName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 제공자: " + providerName);
    }
}

package kr.co.amateurs.server.fixture.auth;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OAuth2TestFixture {

    public static final String DEFAULT_GITHUB_PROVIDER_ID = "12345678";
    public static final String DEFAULT_GITHUB_EMAIL = "github@test.com";
    public static final String DEFAULT_GITHUB_LOGIN = "github_user";
    public static final String DEFAULT_GITHUB_NAME = "KimGitHub";
    public static final String DEFAULT_AVATAR_URL = "https://github.com";


    public static OAuth2User createGitHubOAuth2User(String id, String email, String login, String name) {
        Map<String, Object> attributes = new HashMap<>();
        if (id != null) {
            attributes.put("id", id);
        }
        if (email != null) {
            attributes.put("email", email);
        }
        if (login != null) {
            attributes.put("login", login);
        }
        if (name != null) {
            attributes.put("name", name);
        }
        attributes.put("avatar_url", DEFAULT_AVATAR_URL);

        return new DefaultOAuth2User(null, attributes, "id");
    }

    public static OAuth2User createDefaultGitHubOAuth2User() {
        return createGitHubOAuth2User(
                DEFAULT_GITHUB_PROVIDER_ID,
                DEFAULT_GITHUB_EMAIL,
                DEFAULT_GITHUB_LOGIN,
                DEFAULT_GITHUB_NAME
        );
    }

    public static User.UserBuilder defaultGitHubUser() {
        return User.builder()
                .providerId(DEFAULT_GITHUB_PROVIDER_ID)
                .providerType(ProviderType.GITHUB)
                .email(DEFAULT_GITHUB_EMAIL)
                .nickname(generateUniqueNickname(DEFAULT_GITHUB_LOGIN))
                .name(DEFAULT_GITHUB_NAME)
                .imageUrl(DEFAULT_AVATAR_URL)
                .role(Role.GUEST);
    }

    public static String generateUniqueNickname(String originalNickname) {
        if (originalNickname == null || originalNickname.isEmpty()) {
            originalNickname = "사용자";
        }

        if (originalNickname.length() > 8) {
            originalNickname = originalNickname.substring(0, 8);
        }

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        return originalNickname + "_" + uniqueSuffix;
    }
}

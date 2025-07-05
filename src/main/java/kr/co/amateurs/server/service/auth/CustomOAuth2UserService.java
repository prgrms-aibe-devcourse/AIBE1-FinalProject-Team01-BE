package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RestClient restClient;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 시도: provider={}", provider);
        log.info("{} OAuth2 로그인 사용자 정보 획득 완료", provider);

        String providerId = getProviderId(oAuth2User, provider);
        String email = getEmailWithFallback(userRequest, oAuth2User, provider, providerId);
        String nickname = getNickname(oAuth2User, provider);
        String name = getName(oAuth2User, provider);
        String imageUrl = getImageUrl(oAuth2User, provider);

        try {
            saveOrUpdateUser(provider, providerId, email, nickname, name, imageUrl);
        } catch (Exception e) {
            log.error("OAuth 사용자 등록 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_registration_error"),
                    ErrorCode.OAUTH_USER_REGISTRATION_FAILED.getMessage());
        }

        User user = userRepository.findByProviderIdAndProviderType(
                providerId, ProviderType.valueOf(provider.toUpperCase()))
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private void saveOrUpdateUser(String provider, String providerId, String email, String nickname, String name, String imageUrl) {

        Optional<User> existingUser = userRepository.findByProviderIdAndProviderType (providerId, ProviderType.valueOf(provider.toUpperCase()));

        if (existingUser.isPresent()) {
            log.info("기존 사용자 로그인: userId={}", existingUser.get().getId());
            return;
        }

        if (email != null && !email.isEmpty()) {
            if (userRepository.existsByEmail(email)) {
                log.warn("이미 다른 계정으로 가입된 이메일: {}", email);
                throw ErrorCode.OAUTH_EMAIL_ALREADY_EXISTS.get();
            }
        }

        String uniqueNickname = generateUniqueNickname(nickname);

        User newUser = User.builder()
                .providerId(providerId)
                .providerType(ProviderType.valueOf(provider.toUpperCase()))
                .email(email)
                .nickname(uniqueNickname)
                .name(name)
                .imageUrl(imageUrl)
                .role(Role.GUEST)
                .build();

        userRepository.save(newUser);
        log.info("신규 사용자 등록: userId={}, 닉네임={}", newUser.getId(), uniqueNickname);
    }


    private String generateUniqueNickname (String originalNickname) {
        if (originalNickname == null || originalNickname.isEmpty()) {
            originalNickname = "사용자";
        }

        if (originalNickname.length() > 8) {
            originalNickname = originalNickname.substring(0, 8);
        }

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        return String.format("%s_%s", originalNickname, uniqueSuffix);
    }

    private String generateFakeEmail(String provider, String providerId) {
        return String.format("%s_%s@amateurs.local", provider, providerId);
    }

    private String getProviderId(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = validateAndGetAttributes(oAuth2User, provider);

        Object id = attributes.get("id");
        if (id == null) {
            log.error("{}에서 사용자 ID를 받지 못했습니다", provider);
            throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
        }

        return String.valueOf(id);
    }

    private String getEmailWithFallback(OAuth2UserRequest userRequest, OAuth2User oAuth2User, String provider, String providerId) {
        if (ProviderType.KAKAO.getProviderName().equalsIgnoreCase(provider)) {
            return getKakaoEmail(oAuth2User, providerId);
        }

        Map<String, Object> attributes = validateAndGetAttributes(oAuth2User, provider);

        String attributeEmail = (String) attributes.get("email");
        if (attributeEmail != null && !attributeEmail.isEmpty()) {
            return attributeEmail;
        }

        log.info("GitHub attributes에 이메일이 없어 AccessToken으로 조회 시도");
        try {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            return fetchEmailWithAccessToken(accessToken);
        } catch (CustomException e) {
            log.warn("GitHub API로 이메일 조회 실패: {}", e.getMessage());
        }

        log.info("이메일 조회 실패, generateFakeEmail 사용");
        return generateFakeEmail(provider, providerId);
    }

    private String getKakaoEmail(OAuth2User oAuth2User, String providerId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Map<String, Object> kakaoAccount = getKakaoAccount(attributes);
        if (kakaoAccount == null) {
            log.warn("Kakao 계정 정보가 Map 형식이 아닙니다");
            return generateFakeEmail("kakao", providerId);
        }

        String email = (String) kakaoAccount.get("email");
        if (email != null && !email.isEmpty()) {
            return email;
        }

        log.warn("Kakao에서 이메일을 받지 못했습니다");
        return generateFakeEmail("kakao", providerId);
    }

    private String fetchEmailWithAccessToken(String accessToken) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "amateurs-oauth-app")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (emails == null || emails.isEmpty()) {
                log.warn("GitHub API에서 이메일 목록이 비어있음");
                throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();
            }

            for (Map<String, Object> emailEntry : emails) {
                Boolean isPrimary = (Boolean) emailEntry.get("primary");
                Boolean isVerified = (Boolean) emailEntry.get("verified");

                if (Boolean.TRUE.equals(isPrimary) && Boolean.TRUE.equals(isVerified)) {
                    return (String) emailEntry.get("email");
                }
            }

            for (Map<String, Object> emailEntry : emails) {
                Boolean isVerified = (Boolean) emailEntry.get("verified");

                if (Boolean.TRUE.equals(isVerified)) {
                    return (String) emailEntry.get("email");
                }
            }

            log.warn("GitHub API에서 verified된 이메일을 찾을 수 없음");
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();

        } catch (HttpClientErrorException e) {
            log.error("GitHub API 클라이언트 오류 ({}): {}", e.getStatusCode(), e.getMessage());
            throw e.getStatusCode().value() == 401 ? ErrorCode.UNAUTHORIZED.get() : ErrorCode.ACCESS_DENIED.get();
        } catch (HttpServerErrorException e) {
            log.error("GitHub API 서버 오류 ({}): {}", e.getStatusCode(), e.getMessage());
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();
        } catch (Exception e) {
            log.error("GitHub API 호출 중 오류: {}", e.getMessage());
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();
        }
    }

    private String getNickname(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("github".equals(provider)) {
            String nickname = (String) attributes.get("login");
            if (nickname == null) {
                log.error("GitHub에서 닉네임을 받지 못했습니다");
                throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
            }
            return nickname;
        } else if ("kakao".equals(provider)) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            if (profile == null) {
                return "카카오사용자";
            }

            String nickname = (String) profile.get("nickname");
            return nickname != null ? nickname : "카카오사용자";
        }

        throw ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED.get();
    }

    private String getName(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("github".equals(provider)) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("login");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            return profile != null ? (String) profile.get("nickname") : null;
        }

        return null;
    }

    private String getImageUrl(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("github".equals(provider)) {
            return (String) attributes.get("avatar_url");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            return profile != null ? (String) profile.get("profile_image_url") : null;
        }

        return null;
    }

    private Map<String, Object> validateAndGetAttributes(OAuth2User oAuth2User, String provider) {
        if (!ProviderType.GITHUB.getProviderName().equalsIgnoreCase(provider) &&
                !ProviderType.KAKAO.getProviderName().equalsIgnoreCase(provider)) {
            log.error("지원하지 않는 OAuth Provider: {}", provider);
            throw ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED.get();
        }

        return oAuth2User.getAttributes();
    }

    private Map<String, Object> getKakaoProfile(Map<String, Object> attributes) {
        Object kakaoAccountObj = attributes.get("kakao_account");
        if (!(kakaoAccountObj instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;

        Object profileObj = kakaoAccount.get("profile");
        if (!(profileObj instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) profileObj;

        return profile;
    }

    private Map<String, Object> getKakaoAccount(Map<String, Object> attributes) {
        Object kakaoAccountObj = attributes.get("kakao_account");
        if (!(kakaoAccountObj instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;

        return kakaoAccount;
    }

}

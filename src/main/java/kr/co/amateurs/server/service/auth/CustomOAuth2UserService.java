package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.auth.OAuthUserInfo;
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

import java.util.*;


@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RestClient restClient;

    private static final int MAX_NICKNAME_RETRY = 5;
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://via.placeholder.com/150/cccccc/969696?text=Profile";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String providerName = userRequest.getClientRegistration().getRegistrationId();
        ProviderType providerType = ProviderType.fromProviderName(providerName);

        log.info("OAuth2 로그인 시도: provider={}", providerType.getProviderName());
        log.info("{} OAuth2 로그인 사용자 정보 획득 완료", providerType.getProviderName());

        try {
            OAuthUserInfo userInfo = extractUserInfo(userRequest, oAuth2User, providerType);
            User user = saveOrGetUser(userInfo);

            return new CustomUserDetails(user, oAuth2User.getAttributes());
        } catch (Exception e) {
            log.error("OAuth 사용자 등록 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_registration_error"),
                    ErrorCode.OAUTH_USER_REGISTRATION_FAILED.getMessage());
        }
    }

    private OAuthUserInfo extractUserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User, ProviderType providerType) {
        String providerId = getProviderId(oAuth2User, providerType);
        String email = getEmailWithFallback(userRequest, oAuth2User, providerType, providerId);
        String nickname = getNickname(oAuth2User, providerType);
        String name = getName(oAuth2User, providerType);
        String imageUrl = getImageUrl(oAuth2User, providerType);

        return OAuthUserInfo.builder()
                .providerType(providerType)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .name(name)
                .imageUrl(imageUrl)
                .build();
    }

    private User saveOrGetUser(OAuthUserInfo userInfo) {

        Optional<User> existingUser = userRepository.findByProviderIdAndProviderType(
                userInfo.providerId(), userInfo.providerType());

        if (existingUser.isPresent()) {
            log.info("기존 사용자 로그인: userId={}", existingUser.get().getId());
            return existingUser.get();
        }

        if (userInfo.email() != null && !userInfo.email().isEmpty()) {
            if (userRepository.existsByEmail(userInfo.email())) {
                log.warn("이미 다른 계정으로 가입된 이메일: {}", userInfo.email());
                throw ErrorCode.OAUTH_EMAIL_ALREADY_EXISTS.get();
            }
        }

        String uniqueNickname = generateUniqueNickname(userInfo.nickname());

        User newUser = User.builder()
                .providerId(userInfo.providerId())
                .providerType(userInfo.providerType())
                .email(userInfo.email())
                .nickname(uniqueNickname)
                .name(userInfo.name())
                .imageUrl(userInfo.imageUrl())
                .role(Role.GUEST)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("신규 사용자 등록: userId={}, 닉네임={}", savedUser.getId(), uniqueNickname);

        return savedUser;
    }


    private String generateUniqueNickname (String originalNickname) {
        if (originalNickname == null || originalNickname.isEmpty()) {
            originalNickname = "사용자";
        }

        if (originalNickname.length() > 8) {
            originalNickname = originalNickname.substring(0, 8);
        }


        String baseNickname = originalNickname;

        for (int attempt = 0; attempt < MAX_NICKNAME_RETRY; attempt++) {
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
            String candidateNickname = String.format("%s_%s", baseNickname, uniqueSuffix);

            if (!userRepository.existsByNickname(candidateNickname)) {
                return candidateNickname;
            }

            log.warn("닉네임 중복 발생, 재시도 {}/{}: {}", attempt + 1, MAX_NICKNAME_RETRY, candidateNickname);
        }

        String fallbackNickname = String.format("%s_%s", baseNickname, System.currentTimeMillis());
        log.warn("닉네임 재시도 한계 도달, 타임스탬프 사용: {}", fallbackNickname);
        return fallbackNickname;
    }

    private String generateFakeEmail(String provider, String providerId) {
        return String.format("%s_%s@amateurs.local", provider, providerId);
    }

    private String getProviderId(OAuth2User oAuth2User, ProviderType providerType) {
        Map<String, Object> attributes = validateAndGetAttributes(oAuth2User, providerType);

        String id = String.valueOf(attributes.get("id"));
        if ("null".equals(id) || id.isEmpty()) {
            log.error("{}에서 사용자 ID를 받지 못했습니다", providerType.getProviderName());
            throw ErrorCode.OAUTH_USER_REGISTRATION_FAILED.get();
        }

        return id;
    }

    private String getEmailWithFallback(OAuth2UserRequest userRequest, OAuth2User oAuth2User, ProviderType providerType, String providerId) {
        if (providerType == ProviderType.KAKAO) {
            return getKakaoEmail(oAuth2User, providerId, providerType);
        }

        Map<String, Object> attributes = validateAndGetAttributes(oAuth2User, providerType);

        String attributeEmail = (String) attributes.get("email");
        if (attributeEmail != null && !attributeEmail.isEmpty()) {
            return attributeEmail;
        }

        log.info("{} attributes에 이메일이 없어 AccessToken으로 조회 시도", providerType.getProviderName());
        try {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            return fetchEmailWithAccessToken(accessToken, providerType);
        } catch (CustomException e) {
            log.warn("{} API로 이메일 조회 실패: {}", providerType.getProviderName(), e.getMessage());
        }

        log.info("이메일 조회 실패, generateFakeEmail 사용");
        return generateFakeEmail(providerType.getProviderName(), providerId);
    }

    private String getKakaoEmail(OAuth2User oAuth2User, String providerId, ProviderType providerType) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Map<String, Object> kakaoAccount = getKakaoAccount(attributes);

        String email = (String) kakaoAccount.get("email");
        if (email != null && !email.isEmpty()) {
            return email;
        }

        log.warn("{}에서 이메일을 받지 못했습니다", providerType.getProviderName());
        return generateFakeEmail(providerType.getProviderName(), providerId);
    }

    private String fetchEmailWithAccessToken(String accessToken, ProviderType providerType) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "amateurs-oauth-app")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (emails == null || emails.isEmpty()) {
                log.warn("{} API에서 이메일 목록이 비어있음", providerType.getProviderName());
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

            log.warn("{} API에서 verified된 이메일을 찾을 수 없음", providerType.getProviderName());
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();

        } catch (HttpClientErrorException e) {
            log.error("{} API 클라이언트 오류 ({}): {}", providerType.getProviderName(), e.getStatusCode(), e.getMessage());
            throw e.getStatusCode().value() == 401 ? ErrorCode.UNAUTHORIZED.get() : ErrorCode.ACCESS_DENIED.get();
        } catch (HttpServerErrorException e) {
            log.error("{} API 서버 오류 ({}): {}", providerType.getProviderName(), e.getStatusCode(), e.getMessage());
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();
        } catch (Exception e) {
            log.error("{} API 호출 중 오류: {}", providerType.getProviderName(), e.getMessage());
            throw ErrorCode.OAUTH_EMAIL_API_CALL_FAILED.get();
        }
    }

    private String getNickname(OAuth2User oAuth2User, ProviderType providerType) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (providerType == ProviderType.GITHUB) {
            String nickname = (String) attributes.get("login");
            return nickname != null ? nickname : "GitHub사용자";
        } else if (providerType == ProviderType.KAKAO) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            String nickname = (String) profile.get("nickname");
            return nickname != null ? nickname : "카카오사용자";
        }

        throw ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED.get();
    }

    private String getName(OAuth2User oAuth2User, ProviderType providerType) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (providerType == ProviderType.GITHUB) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("login");
        } else if (providerType == ProviderType.KAKAO) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            String name = (String) profile.get("nickname");
            return name != null ? name : "카카오사용자";
        }

        return "사용자";
    }

    private String getImageUrl(OAuth2User oAuth2User, ProviderType providerType) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (providerType == ProviderType.GITHUB) {
            String imageUrl = (String) attributes.get("avatar_url");
            return imageUrl != null ? imageUrl : DEFAULT_PROFILE_IMAGE_URL;
        } else if (providerType == ProviderType.KAKAO) {
            Map<String, Object> profile = getKakaoProfile(attributes);
            String imageUrl = (String) profile.get("profile_image_url");
            return imageUrl != null ? imageUrl : DEFAULT_PROFILE_IMAGE_URL;
        }

        return DEFAULT_PROFILE_IMAGE_URL;
    }

    private Map<String, Object> validateAndGetAttributes(OAuth2User oAuth2User, ProviderType providerType) {
        if (providerType != ProviderType.GITHUB && providerType != ProviderType.KAKAO) {
            log.error("지원하지 않는 OAuth Provider: {}", providerType.getProviderName());
            throw ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED.get();
        }

        return oAuth2User.getAttributes();
    }

    private Map<String, Object> getKakaoProfile(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = getKakaoAccount(attributes);

        try {
            Object profileObj = kakaoAccount.get("profile");
            if (profileObj == null) {
                return Collections.emptyMap();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) profileObj;
            return profile;
        } catch (ClassCastException e) {
            log.warn("카카오 프로필 정보 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> getKakaoAccount(Map<String, Object> attributes) {
        try {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj == null) {
                return Collections.emptyMap();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;

            return kakaoAccount;
        } catch (ClassCastException e) {
            log.warn("카카오 계정 정보 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
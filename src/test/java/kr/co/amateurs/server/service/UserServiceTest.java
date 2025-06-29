package kr.co.amateurs.server.service;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.TestAuthHelper;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.user.UserBasicProfileEditRequestDto;
import kr.co.amateurs.server.domain.dto.user.UserBasicProfileEditResponseDto;
import kr.co.amateurs.server.domain.dto.user.UserProfileResponseDto;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = UserTestFixture.defaultUser()
                .email(UserTestFixture.generateUniqueEmail())
                .nickname(UserTestFixture.generateUniqueNickname())
                .password(passwordEncoder.encode(UserTestFixture.DEFAULT_PASSWORD))
                .role(Role.GUEST)
                .imageUrl("https://example.com/profile.jpg")
                .build();
        testUser.addUserTopics(Set.of(Topic.FRONTEND, Topic.BACKEND));

        testUser = TestAuthHelper.setAuthentication(testUser, userRepository);
    }

    @Test
    void 사용자_엔티티를_프로필_응답_DTO로_올바르게_변환한다() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        UserProfileResponseDto result = UserProfileResponseDto.from(savedUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo(savedUser.getEmail());
        assertThat(result.nickname()).isEqualTo(savedUser.getNickname());
        assertThat(result.name()).isEqualTo(savedUser.getName());
        assertThat(result.topics()).containsExactlyInAnyOrder(Topic.FRONTEND, Topic.BACKEND);
    }

    @Test
    void 토픽_Lazy_Loading이_정상_작동한다() {
        // given
        User savedUser = userRepository.save(testUser);

        // when
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // then
        assertThatNoException().isThrownBy(() -> {
            Set<Topic> topics = foundUser.getUserTopics().stream()
                    .map(UserTopic::getTopic)
                    .collect(Collectors.toSet());

            assertThat(topics).hasSize(2);
            assertThat(topics).containsExactlyInAnyOrder(Topic.FRONTEND, Topic.BACKEND);
        });
    }

    @Test
    void 기본_정보_수정_요청_시_정상적으로_업데이트된다() {
        // given
        UserBasicProfileEditRequestDto request = UserBasicProfileEditRequestDto.builder()
                .name("변경된이름")
                .nickname("changedNick")
                .imageUrl("https://example.com/new-profile.jpg")
                .build();

        // when
        UserBasicProfileEditResponseDto response = userService.updateBasicProfile(request);

        // then
        assertThat(response.name()).isEqualTo("변경된이름");
        assertThat(response.nickname()).isEqualTo("changedNick");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/new-profile.jpg");

        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("변경된이름");
        assertThat(updatedUser.getNickname()).isEqualTo("changedNick");
        assertThat(updatedUser.getImageUrl()).isEqualTo("https://example.com/new-profile.jpg");
    }
}
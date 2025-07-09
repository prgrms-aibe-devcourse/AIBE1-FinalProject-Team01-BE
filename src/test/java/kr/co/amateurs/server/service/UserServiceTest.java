package kr.co.amateurs.server.service;

import kr.co.amateurs.server.config.EmbeddedRedisConfig;
import kr.co.amateurs.server.config.TestAuthHelper;
import kr.co.amateurs.server.domain.dto.user.*;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.common.UserTestFixture;
import kr.co.amateurs.server.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

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
        UserProfileResponseDTO result = UserProfileResponseDTO.from(savedUser);

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
        String uniqueNickname = UserTestFixture.generateUniqueNickname();
        UserBasicProfileEditRequestDTO request = UserBasicProfileEditRequestDTO.builder()
                .name("변경된이름")
                .nickname(uniqueNickname)
                .imageUrl("https://example.com/new-profile.jpg")
                .build();

        // when
        UserBasicProfileEditResponseDTO response = userService.updateBasicProfile(request);

        // then
        assertThat(response.name()).isEqualTo("변경된이름");
        assertThat(response.nickname()).isEqualTo(uniqueNickname);
        assertThat(response.imageUrl()).isEqualTo("https://example.com/new-profile.jpg");

        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("변경된이름");
        assertThat(updatedUser.getNickname()).isEqualTo(uniqueNickname);
        assertThat(updatedUser.getImageUrl()).isEqualTo("https://example.com/new-profile.jpg");
    }

    @Test
    void 올바른_비밀번호로_변경_시_정상적으로_업데이트된다() {
        // given
        UserPasswordEditRequestDTO request = UserPasswordEditRequestDTO.builder()
                .currentPassword(UserTestFixture.DEFAULT_PASSWORD)
                .newPassword("newPassword123")
                .build();

        // when
        UserPasswordEditResponseDTO response = userService.updatePassword(request);

        // then
        assertThat(response.message()).isEqualTo("비밀번호가 성공적으로 변경되었습니다");

        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword123", updatedUser.getPassword())).isTrue();
    }

    @Test
    void 잘못된_현재_비밀번호_입력_시_예외가_발생한다() {
        // given
        UserPasswordEditRequestDTO request = UserPasswordEditRequestDTO.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword123")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 유효한_토픽_목록으로_변경_시_정상적으로_업데이트된다() {
        // given
        UserTopicsEditRequestDTO request = UserTopicsEditRequestDTO.builder()
                .topics(Set.of(Topic.BACKEND, Topic.DATA, Topic.MOBILE))
                .build();

        // when
        UserTopicsEditResponseDTO response = userService.updateTopics(request);

        // then
        assertThat(response.topics()).hasSize(3);
        assertThat(response.topics()).containsExactlyInAnyOrder(Topic.BACKEND, Topic.DATA, Topic.MOBILE);

        User updatedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(updatedUser.getUserTopics()).hasSize(3);
    }

    @Test
    void 빈_토픽_목록으로_변경_시_예외가_발생한다() {
        // given
        UserTopicsEditRequestDTO request = UserTopicsEditRequestDTO.builder()
                .topics(Set.of())
                .build();

        // when & then
        assertThatThrownBy(() -> userService.updateTopics(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 토픽_4개_이상_변경_시_예외가_발생한다() {
        // given
        UserTopicsEditRequestDTO request = UserTopicsEditRequestDTO.builder()
                .topics(Set.of(Topic.FRONTEND, Topic.DEVOPS, Topic.AI, Topic.BACKEND))
                .build();

        // when & then
        assertThatThrownBy(() -> userService.updateTopics(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 올바른_비밀번호로_탈퇴_시_정상적으로_처리된다() {
        // given
        UserDeleteRequestDTO request = new UserDeleteRequestDTO(UserTestFixture.DEFAULT_PASSWORD);

        // when
        UserDeleteResponseDTO response = userService.deleteUser(request);

        // then
        assertThat(response.message()).isEqualTo("회원 탈퇴가 성공적으로 처리되었습니다");

        User deletedUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();
        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
    }
}
package kr.co.amateurs.server.service.follow;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.follow.FollowRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static kr.co.amateurs.server.fixture.follow.FollowTestFixture.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FollowServiceTest {

    @Autowired private FollowRepository followRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private PostStatisticsRepository postStatisticsRepository;
    @Autowired private FollowService followService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        // 사용자, 게시글, 통계 전부 저장
        userRepository.save(DefaultUsers.CURRENT_USER);
        userRepository.save(DefaultUsers.TARGET_USER_1);
        userRepository.save(DefaultUsers.TARGET_USER_2);
        userRepository.save(DefaultUsers.TARGET_USER_3);
        userRepository.save(DefaultUsers.FOLLOWER_USER_1);
        userRepository.save(DefaultUsers.FOLLOWER_USER_2);

        postRepository.save(DefaultPosts.FREE_POST);
        postRepository.save(DefaultPosts.REVIEW_POST);
        postRepository.save(DefaultPosts.PROJECT_POST);
        postRepository.save(DefaultPosts.NEWS_POST);
        postRepository.save(DefaultPosts.QNA_POST);
        postRepository.save(DefaultPosts.RETRO_POST);

        postStatisticsRepository.save(DefaultStatistics.FREE_STATS);
        postStatisticsRepository.save(DefaultStatistics.REVIEW_STATS);
        postStatisticsRepository.save(DefaultStatistics.PROJECT_STATS);
        postStatisticsRepository.save(DefaultStatistics.NEWS_STATS);
        postStatisticsRepository.save(DefaultStatistics.QNA_STATS);
        postStatisticsRepository.save(DefaultStatistics.RETRO_STATS);
    }

    @Nested @DisplayName("팔로워 목록 조회")
    class GetFollowerListTest {
        @Test @DisplayName("정상 조회")
        void success() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);
            followRepository.save(DefaultFollows.F1_FOLLOWS_CURRENT);
            followRepository.save(DefaultFollows.F2_FOLLOWS_CURRENT);

            var result = followService.getFollowerList();

            assertThat(result).hasSize(2)
                    .extracting("nickname")
                    .containsExactlyInAnyOrder("follower1", "follower2");
        }

        @Test @DisplayName("없으면 빈 리스트")
        void empty() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            var result = followService.getFollowerList();

            assertThat(result).isEmpty();
        }
    }

    @Nested @DisplayName("팔로잉 목록 조회")
    class GetFollowingListTest {
        @Test @DisplayName("정상 조회")
        void success() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);
            followRepository.save(DefaultFollows.CURRENT_FOLLOWS_T1);
            followRepository.save(DefaultFollows.CURRENT_FOLLOWS_T2);
            followRepository.save(DefaultFollows.CURRENT_FOLLOWS_T3);

            var result = followService.getFollowingList();

            assertThat(result).hasSize(3)
                    .extracting("nickname")
                    .containsExactlyInAnyOrder("targetUser1", "targetUser2", "targetUser3");
        }

        @Test @DisplayName("없으면 빈 리스트")
        void empty() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            var result = followService.getFollowingList();

            assertThat(result).isEmpty();
        }
    }

    @Nested @DisplayName("팔로우 게시글 조회")
    class GetFollowPostListTest {
        @Test @DisplayName("학생은 모든 게시글 조회")
        void studentSeesAll() {
            var student = createStudentUser("stu@test.com", "student");
            userRepository.save(student);
            when(userService.getCurrentLoginUser()).thenReturn(student);

            followRepository.save(createFollow(student, DefaultUsers.TARGET_USER_1));
            followRepository.save(createFollow(student, DefaultUsers.TARGET_USER_2));

            var result = followService.getFollowPostList();

            // TARGET_USER_1: 3개, TARGET_USER_2: 2개 총 5
            assertThat(result).hasSize(5)
                    .extracting("title")
                    .containsExactlyInAnyOrder(
                            "자유 게시글", "프로젝트 게시글", "회고 게시글",
                            "리뷰 게시글", "Q&A 게시글"
                    );
        }

        @Test @DisplayName("게스트는 일부만 조회")
        void guestSeesPartial() {
            var guest = createGuestUser("gst@test.com", "guest");
            userRepository.save(guest);
            when(userService.getCurrentLoginUser()).thenReturn(guest);

            followRepository.save(createFollow(guest, DefaultUsers.TARGET_USER_1));
            followRepository.save(createFollow(guest, DefaultUsers.TARGET_USER_2));

            var result = followService.getFollowPostList();

            assertThat(result).hasSize(4)
                    .extracting("title")
                    .containsExactlyInAnyOrder(
                            "자유 게시글", "프로젝트 게시글",
                            "회고 게시글", "리뷰 게시글", "Q&A 게시글"
                    )
                    .doesNotContain("뉴스 게시글");
        }

        @Test @DisplayName("익명은 제한된 게시글만 조회")
        void anonSeesLimited() {
            var anon = createAnonymousUser("ano@test.com", "anonymous");
            userRepository.save(anon);
            when(userService.getCurrentLoginUser()).thenReturn(anon);

            followRepository.save(createFollow(anon, DefaultUsers.TARGET_USER_1));
            followRepository.save(createFollow(anon, DefaultUsers.TARGET_USER_2));
            followRepository.save(createFollow(anon, DefaultUsers.TARGET_USER_3));

            var result = followService.getFollowPostList();

            assertThat(result).hasSize(3)
                    .extracting("title")
                    .containsExactlyInAnyOrder(
                            "프로젝트 게시글", "리뷰 게시글", "뉴스 게시글"
                    );
        }

        @Test @DisplayName("팔로잉만 있고 게시글 없으면 빈 리스트")
        void noPosts() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);
            var userNoPosts = createStudentUser("np@test.com", "noposts");
            userRepository.save(userNoPosts);
            followRepository.save(createFollow(DefaultUsers.CURRENT_USER, userNoPosts));

            var result = followService.getFollowPostList();
            assertThat(result).isEmpty();
        }
    }

    @Nested @DisplayName("팔로우 추가/삭제")
    class AddRemoveTest {
        @Test @DisplayName("추가 성공")
        void followSuccess() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);
            var dto = followService.followUser(DefaultUsers.TARGET_USER_1.getId());

            assertThat(dto.nickname()).isEqualTo("targetUser1");
            assertThat(dto.userId()).isEqualTo(DefaultUsers.TARGET_USER_1.getId());

            var saved = followRepository.findByFromUser(DefaultUsers.CURRENT_USER);
            assertThat(saved).hasSize(1);
        }

        @Test @DisplayName("자기 자신 팔로우 → 예외")
        void followSelf() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            assertThatThrownBy(() -> followService.followUser(DefaultUsers.CURRENT_USER.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.SELF_FOLLOW.getMessage());
        }

        @Test @DisplayName("없는 사용자 팔로우 → 예외")
        void followNotFound() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            assertThatThrownBy(() -> followService.followUser(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test @DisplayName("삭제 성공")
        void unfollowSuccess() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);
            followRepository.save(createFollow(DefaultUsers.CURRENT_USER, DefaultUsers.TARGET_USER_1));

            followService.unfollowUser(DefaultUsers.TARGET_USER_1.getId());
            assertThat(followRepository.findByFromUser(DefaultUsers.CURRENT_USER)).isEmpty();
        }

        @Test @DisplayName("없는 사용자 언팔로우 → 예외")
        void unfollowNotFound() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            assertThatThrownBy(() -> followService.unfollowUser(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test @DisplayName("관계 없어도 삭제 정상 처리")
        void unfollowNoRelation() {
            when(userService.getCurrentLoginUser()).thenReturn(DefaultUsers.CURRENT_USER);

            followService.unfollowUser(DefaultUsers.TARGET_USER_1.getId());
            assertThat(followRepository.findByFromUser(DefaultUsers.CURRENT_USER)).isEmpty();
        }
    }
}

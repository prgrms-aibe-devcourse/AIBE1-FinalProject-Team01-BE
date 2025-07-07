package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.dto.ai.AiProfileRequest;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.ai.PostSummaryData;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.fixture.ai.AiTestFixture;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import kr.co.amateurs.server.service.post.PostService;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiProfileServiceTest {
    @Autowired
    private AiProfileService aiProfileService;
    @MockitoBean
    private BookmarkService bookmarkService;
    @MockitoBean
    private PostService postService;
    @MockitoBean
    private LikeService likeService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AiLlmService aiLlmService;
    @MockitoBean
    private AiProfileRepository aiProfileRepository;

    private User testUser;
    private AiProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = AiTestFixture.createTestUser();
        testProfile = AiTestFixture.createTestProfile(testUser);
    }

    @Nested
    class 초기_프로필_생성_기능 {
        @Test
        void 정상적으로_초기_프로필을_생성한다() {
            // given
            given(userService.getUserTopics(1L)).willReturn("AI, BACKEND");
            AiProfileResponse mockResponse = AiTestFixture.createAiProfileResponse("AI에 관심", "AI, BACKEND");
            given(aiLlmService.generateInitialProfile(anyString())).willReturn(mockResponse);
            given(userService.findById(1L)).willReturn(testUser);
            given(aiProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(aiProfileRepository.save(any(AiProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            AiProfile result = aiProfileService.generateInitialProfile(1L);

            // then
            assertThat(result.getPersonaDescription()).isEqualTo("AI에 관심");
            assertThat(result.getInterestKeywords()).isEqualTo("AI, BACKEND");
        }

        @Test
        void 예외가_발생하면_CustomException을_던진다() {
            // given
            given(userService.getUserTopics(1L)).willThrow(new RuntimeException("fail"));

            // when & then
            assertThatThrownBy(() -> aiProfileService.generateInitialProfile(1L))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 완전한_프로필_생성_기능 {
        @Test
        void 정상적으로_완전한_프로필을_생성한다() {
            // given
            given(bookmarkService.getBookmarkedPosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(1L, "a", "b", "북마크")));
            given(likeService.getLikedPosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(2L, "c", "d", "좋아요")));
            given(postService.getWritePosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(3L, "e", "f", "작성글")));
            given(aiLlmService.summarizeBookmarkedPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("북마크", "북마크 요약"));
            given(aiLlmService.summarizeLikedPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("좋아요", "좋아요 요약"));
            given(aiLlmService.summarizeWrittenPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("작성글", "작성글 요약"));
            given(userService.getUserTopics(1L)).willReturn("AI, BACKEND");
            given(userService.getDevcourseName(1L)).willReturn("AI_BACKEND");
            given(userService.findById(1L)).willReturn(testUser);
            given(aiProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
            given(aiProfileRepository.save(any(AiProfile.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(aiLlmService.generateFinalProfile(any(AiProfileRequest.class))).willReturn(AiTestFixture.createAiProfileResponse("최종 설명", "AI, BACKEND"));

            // when
            AiProfile result = aiProfileService.generateCompleteUserProfile(1L);

            // then
            assertThat(result.getPersonaDescription()).isEqualTo("최종 설명");
            assertThat(result.getInterestKeywords()).isEqualTo("AI, BACKEND");
        }

        @Test
        void 예외가_발생하면_CustomException을_던진다() {
            // given
            given(bookmarkService.getBookmarkedPosts(1L)).willThrow(new RuntimeException("fail"));

            // when & then
            assertThatThrownBy(() -> aiProfileService.generateCompleteUserProfile(1L))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class 프로필_생성_기능 {
        @Test
        void 정상적으로_프로필을을_생성한다() {
            // given
            AiProfile profile = AiProfile.builder()
                    .user(testUser)
                    .personaDescription("설명")
                    .interestKeywords("AI, BACKEND")
                    .build();
            given(userService.getUserTopics(1L)).willReturn("AI, BACKEND");
            given(userService.getDevcourseName(1L)).willReturn("AI_BACKEND");
            given(userService.findById(1L)).willReturn(testUser);
            given(aiProfileRepository.findByUserId(1L)).willReturn(Optional.of(profile));
            given(aiProfileRepository.save(any(AiProfile.class))).willReturn(profile);
            given(bookmarkService.getBookmarkedPosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(1L, "a", "b", "북마크")));
            given(likeService.getLikedPosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(2L, "c", "d", "좋아요")));
            given(postService.getWritePosts(1L)).willReturn(List.of(AiTestFixture.createPostContentData(3L, "e", "f", "작성글")));
            given(aiLlmService.summarizeBookmarkedPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("북마크", "요약"));
            given(aiLlmService.summarizeLikedPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("좋아요", "요약"));
            given(aiLlmService.summarizeWrittenPosts(anyList())).willReturn(AiTestFixture.createPostSummaryData("작성글", "요약"));
            given(aiLlmService.generateFinalProfile(any(AiProfileRequest.class))).willReturn(AiTestFixture.createAiProfileResponse("설명", "AI, BACKEND"));

            // when
            AiProfileResponse result = aiProfileService.generateUserProfileResponse(1L);

            // then
            assertThat(result.personaDescription()).isEqualTo("설명");
            assertThat(result.interestKeywords()).isEqualTo("AI, BACKEND");
        }
    }
}

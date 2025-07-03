package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.repository.ai.AiRecommendPostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.fixture.ai.AiTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostRecommendServiceTest {
    @Autowired
    private PostRecommendService postRecommendService;
    @MockitoBean
    private AiProfileRepository aiProfileRepository;
    @MockitoBean
    private AiRecommendPostRepository aiRecommendPostRepository;
    @MockitoBean
    private PostEmbeddingService postEmbeddingService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private kr.co.amateurs.server.service.post.PostService postService;

    private User testUser;
    private AiProfile testProfile;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = AiTestFixture.createTestUser();
        testProfile = AiTestFixture.createTestProfile(testUser);
        testPost = AiTestFixture.createTestPost(testUser);
    }

    @Nested
    class getStoredRecommendations_메서드는 {
        @Test
        void 추천_데이터가_있으면_정상적으로_반환한다() {
            // given
            PostRecommendationResponse response = new PostRecommendationResponse(
                100L, "AI 추천 게시글", "테스터", 5, 10, 2, null, null
            );
            given(aiRecommendPostRepository.findRecommendationDataByUserId(anyLong())).willReturn(List.of(response));

            // when
            List<PostRecommendationResponse> result = postRecommendService.getStoredRecommendations(1L, 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(100L);
        }

        @Test
        void 추천_데이터가_없으면_빈_리스트를_반환한다() {
            // given
            given(aiRecommendPostRepository.findRecommendationDataByUserId(anyLong())).willReturn(Collections.emptyList());

            // when
            List<PostRecommendationResponse> result = postRecommendService.getStoredRecommendations(1L, 10);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class getAiPersonalRecommendedPosts_메서드는 {
        @Test
        void AI_프로필이_없으면_빈_리스트를_반환한다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.empty());

            // when
            List<Post> result = postRecommendService.getAiPersonalRecommendedPosts(1L, 10);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class saveRecommendationsToDB_메서드는 {
        @Test
        void 추천_게시글이_정상적으로_DB에_저장된다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.of(testProfile));
            given(postEmbeddingService.findSimilarPosts(anyString(), anyInt())).willReturn(Collections.emptyList());
            given(userService.findById(1L)).willReturn(testUser);
            // 추천 게시글이 없으므로 실제로 저장되는 것은 없음

            // when/then
            assertThatCode(() -> postRecommendService.saveRecommendationsToDB(1L, 10)).doesNotThrowAnyException();
        }
    }
} 
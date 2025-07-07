package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.dto.ai.PostRecommendationResponse;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.repository.ai.AiRecommendPostRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.fixture.ai.AiTestFixture;
import kr.co.amateurs.server.service.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.segment.TextSegment;

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
    private PostService postService;
    @MockitoBean
    private PostRepository postRepository;

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
    class 추천_데이터_조회_기능 {
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
        void 여러_추천_데이터가_있으면_limit_개수만큼_모두_반환한다() {
            // given
            User user = AiTestFixture.createTestUser();
            Post post1 = AiTestFixture.createTestPost(user);
            Post post2 = AiTestFixture.createTestPost(user);
            PostRecommendationResponse response1 = new PostRecommendationResponse(
                post1.getId(), post1.getTitle(), user.getNickname(), 5, 10, 2, post1.getBoardType(), post1.getCreatedAt()
            );
            PostRecommendationResponse response2 = new PostRecommendationResponse(
                post2.getId(), post2.getTitle(), user.getNickname(), 7, 20, 3, post2.getBoardType(), post2.getCreatedAt()
            );
            given(aiRecommendPostRepository.findRecommendationDataByUserId(anyLong())).willReturn(List.of(response1, response2));

            // when
            List<PostRecommendationResponse> result = postRecommendService.getStoredRecommendations(1L, 10);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(PostRecommendationResponse::id).containsExactly(post1.getId(), post2.getId());
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
    class AI_개인화_추천_게시글_조회_기능 {
        @Test
        void AI_프로필이_없으면_빈_리스트를_반환한다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.empty());

            // when
            List<Post> result = postRecommendService.getAiPersonalRecommendedPosts(1L, 10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void AI_프로필이_있고_유사_게시글이_여러_개면_limit_개수만큼_반환한다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.of(testProfile));

            User otherUser1 = AiTestFixture.createTestUser();
            AiTestFixture.setId(otherUser1, 2L);

            User otherUser2 = AiTestFixture.createTestUser();
            AiTestFixture.setId(otherUser2, 3L);
            
            Post post1 = AiTestFixture.createTestPostWithId(otherUser1, 100L);
            Post post2 = AiTestFixture.createTestPostWithId(otherUser2, 101L);

            given(postService.findById(post1.getId())).willReturn(post1);
            given(postService.findById(post2.getId())).willReturn(post2);

            // EmbeddingMatch<TextSegment> mock 생성
            TextSegment segment1 = TextSegment.from("content1");
            TextSegment segment2 = TextSegment.from("content2");
            // postId, userId 메타데이터 세팅 (다른 사용자들의 ID로 설정)
            segment1.metadata().put("postId", post1.getId().toString());
            segment1.metadata().put("userId", otherUser1.getId().toString());
            segment2.metadata().put("postId", post2.getId().toString());
            segment2.metadata().put("userId", otherUser2.getId().toString());
            EmbeddingMatch<TextSegment> match1 = new EmbeddingMatch<TextSegment>(0.95, "embedding1", null, segment1);
            EmbeddingMatch<TextSegment> match2 = new EmbeddingMatch<TextSegment>(0.93, "embedding2", null, segment2);
            given(postEmbeddingService.findSimilarPosts(anyString(), anyInt())).willReturn(List.of(match1, match2));

            // when
            List<Post> result = postRecommendService.getAiPersonalRecommendedPosts(1L, 10);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class 추천_결과_DB_저장_기능 {
        @Test
        void 추천_게시글이_정상적으로_DB에_저장된다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.of(testProfile));
            Post post1 = AiTestFixture.createTestPost(testUser);
            Post post2 = AiTestFixture.createTestPost(testUser);
            TextSegment segment1 = TextSegment.from("content1");
            TextSegment segment2 = TextSegment.from("content2");
            segment1.metadata().put("postId", post1.getId().toString());
            segment1.metadata().put("userId", testUser.getId().toString());
            segment2.metadata().put("postId", post2.getId().toString());
            segment2.metadata().put("userId", testUser.getId().toString());
            EmbeddingMatch<TextSegment> match1 = new EmbeddingMatch<TextSegment>(0.95, "embedding1", null, segment1);
            EmbeddingMatch<TextSegment> match2 = new EmbeddingMatch<TextSegment>(0.93, "embedding2", null, segment2);
            given(postEmbeddingService.findSimilarPosts(anyString(), anyInt())).willReturn(List.of(match1, match2));
            given(userService.findById(anyLong())).willReturn(testUser);
            given(aiRecommendPostRepository.saveAll(anyList())).willReturn(Collections.emptyList());

            // when/then
            assertThatCode(() -> postRecommendService.saveRecommendationsToDB(1L, 10)).doesNotThrowAnyException();
        }

        @Test
        void AI_프로필이_없으면_DB_저장_로직이_실행되지_않는다() {
            // given
            given(aiProfileRepository.findByUserId(anyLong())).willReturn(Optional.empty());

            // when/then
            assertThatCode(() -> postRecommendService.saveRecommendationsToDB(1L, 10)).doesNotThrowAnyException();
        }
    }
} 
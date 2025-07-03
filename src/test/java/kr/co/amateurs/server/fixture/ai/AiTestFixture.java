package kr.co.amateurs.server.fixture.ai;

import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.ai.PostSummaryData;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;

public class AiTestFixture {
    public static User createTestUser() {
        User user = User.builder()
                .email("aiuser@test.com")
                .nickname("ai테스터")
                .name("AI유저")
                .password("encodedPassword123")
                .role(Role.STUDENT)
                .build();
        setId(user, 1L);
        return user;
    }

    public static AiProfile createTestProfile(User user) {
        AiProfile profile = AiProfile.builder()
                .user(user)
                .personaDescription("사용자는 AI에 관심이 많습니다")
                .interestKeywords("BACKEND, AI")
                .build();
        setId(profile, 1L);
        return profile;
    }

    public static Post createTestPost(User user) {
        Post post = Post.builder()
                .user(user)
                .title("AI 추천 게시글")
                .content("AI 내용")
                .build();
        setId(post, 100L);
        return post;
    }

    public static RecommendedPost createRecommendedPost(User user, Post post) {
        RecommendedPost rec = RecommendedPost.builder()
                .user(user)
                .post(post)
                .build();
        setId(rec, 200L);
        return rec;
    }

    public static PostContentData createPostContentData(Long postId, String title, String content, String activityType) {
        return new PostContentData(postId, title, content, activityType);
    }

    public static PostSummaryData createPostSummaryData(String type, String summary) {
        return new PostSummaryData(type, summary);
    }

    public static AiProfileResponse createAiProfileResponse(String personaDescription, String interestKeywords) {
        return new AiProfileResponse(personaDescription, interestKeywords);
    }

    private static void setId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 
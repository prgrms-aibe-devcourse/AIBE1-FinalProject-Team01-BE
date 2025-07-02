package kr.co.amateurs.server.fixture.ai;

import kr.co.amateurs.server.domain.dto.ai.*;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.ai.RecommendedPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

public class AiTestFixture {

    // === AiProfile Entity ===
    public static AiProfile.AiProfileBuilder defaultAiProfile() {
        return AiProfile.builder()
                .personaDescription("백엔드 개발에 관심이 많은 개발자입니다. Spring Framework와 JPA를 주로 사용하며, 최근 AI와 머신러닝에도 관심을 보이고 있습니다.")
                .interestKeywords("Java,Spring,JPA,AI,백엔드개발,API설계");
    }

    public static AiProfile createAiProfile(User user) {
        return defaultAiProfile()
                .user(user)
                .build();
    }

    public static AiProfile createAiProfileWithCustomData(User user, String persona, String keywords) {
        return AiProfile.builder()
                .user(user)
                .personaDescription(persona)
                .interestKeywords(keywords)
                .build();
    }

    // === RecommendedPost Entity ===
    public static RecommendedPost createRecommendedPost(User user, Post post) {
        return RecommendedPost.builder()
                .user(user)
                .post(post)
                .build();
    }

    // === DTO Objects ===
    public static AiProfileRequest createAiProfileRequest() {
        return new AiProfileRequest(
                "FRONTEND,BACKEND,AI",
                "백엔드 트랙",
                createPostSummaryDataList()
        );
    }

    public static AiProfileRequest createAiProfileRequestWithCustomData(String topics, String devCourse, List<PostSummaryData> summaries) {
        return new AiProfileRequest(topics, devCourse, summaries);
    }

    public static AiProfileResponse createAiProfileResponse() {
        return new AiProfileResponse(
                "백엔드 개발과 AI에 관심이 많은 학습자입니다. Spring Framework를 활용한 웹 개발과 최신 AI 기술 동향을 추적하며 실무에 적용하려고 노력합니다.",
                "Java,Spring,AI,백엔드,API,데이터베이스"
        );
    }

    public static AiProfileResponse createAiProfileResponseWithCustomData(String persona, String keywords) {
        return new AiProfileResponse(persona, keywords);
    }

    public static PostContentData createPostContentData() {
        return new PostContentData(
                "Spring Boot 실전 가이드",
                "Spring Boot를 활용한 웹 애플리케이션 개발 방법에 대해 설명합니다. JPA와 연동하여 데이터베이스를 효율적으로 관리하는 방법도 포함되어 있습니다."
        );
    }

    public static PostContentData createPostContentDataWithCustomData(String title, String content) {
        return new PostContentData(title, content);
    }

    public static List<PostContentData> createPostContentDataList() {
        return List.of(
                createPostContentDataWithCustomData("Spring Boot 기초", "Spring Boot 프레임워크의 기본 개념과 설정 방법"),
                createPostContentDataWithCustomData("JPA 활용법", "Java Persistence API를 활용한 데이터베이스 연동"),
                createPostContentDataWithCustomData("AI 개발 트렌드", "최신 AI 기술 동향과 개발 방법론")
        );
    }

    public static PostSummaryData createPostSummaryData() {
        return new PostSummaryData(
                "북마크",
                "주로 백엔드 개발과 Spring Framework에 관한 게시글을 북마크하는 경향이 있습니다. API 설계와 데이터베이스 최적화에 특히 관심을 보입니다."
        );
    }

    public static PostSummaryData createPostSummaryDataWithCustomData(String activityType, String summary) {
        return new PostSummaryData(activityType, summary);
    }

    public static List<PostSummaryData> createPostSummaryDataList() {
        return List.of(
                createPostSummaryDataWithCustomData("북마크", "Spring Boot와 JPA 관련 게시글을 주로 북마크"),
                createPostSummaryDataWithCustomData("좋아요", "AI와 머신러닝 관련 기술 포스트에 좋아요 표시"),
                createPostSummaryDataWithCustomData("작성글", "백엔드 API 설계와 성능 최적화에 대한 글 작성")
        );
    }

    public static PostRecommendationResponse createPostRecommendationResponse() {
        return new PostRecommendationResponse(
                1L,
                "Spring Boot 실전 프로젝트",
                "테스트유저",
                5,
                10,
                2,
                "FREE",
                LocalDateTime.now().minusDays(1)
        );
    }

    public static PostRecommendationResponse createPostRecommendationResponseWithCustomData(
            Long postId, String title, String nickname, int likeCount, int viewCount, 
            int commentCount, String boardType, LocalDateTime createdAt) {
        return new PostRecommendationResponse(
                postId, title, nickname, likeCount, viewCount, 
                commentCount, boardType, createdAt
        );
    }

    public static List<PostRecommendationResponse> createPostRecommendationResponseList() {
        return List.of(
                createPostRecommendationResponseWithCustomData(1L, "Spring Security 가이드", "개발자A", 8, 25, 3, "FREE", LocalDateTime.now().minusHours(2)),
                createPostRecommendationResponseWithCustomData(2L, "JPA N+1 문제 해결", "개발자B", 12, 40, 5, "IT", LocalDateTime.now().minusHours(5)),
                createPostRecommendationResponseWithCustomData(3L, "AI 모델 API 연동", "개발자C", 6, 18, 2, "FREE", LocalDateTime.now().minusDays(1))
        );
    }

    // === Test Constants ===
    public static final String DEFAULT_PERSONA = "백엔드 개발에 관심이 많은 데브코스 수강생입니다.";
    public static final String DEFAULT_KEYWORDS = "Java,Spring,Backend,API";
    public static final String DEFAULT_USER_TOPICS = "FRONTEND,BACKEND";
    public static final String DEFAULT_DEVCOURSE_NAME = "백엔드 트랙";
    public static final String DEFAULT_AI_ANALYSIS_RESULT = "AI 분석 결과입니다.";

    // === Empty Data for Edge Cases ===
    public static PostSummaryData createEmptyPostSummaryData(String activityType) {
        return new PostSummaryData(activityType, activityType + " 활동이 없습니다.");
    }

    public static List<PostContentData> createEmptyPostContentDataList() {
        return List.of();
    }
}

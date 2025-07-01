package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class MatchTestFixture {

    // === User 관련 픽스처 ===
    public static User createUser(String email, String nickname, String name) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .name(name)
                .role(Role.STUDENT)
                .providerType(ProviderType.LOCAL)
                .devcourseName(DevCourseTrack.AI_BACKEND)
                .devcourseBatch("1기")
                .imageUrl("https://example.com/profile.jpg")
                .build();
    }

    public static User createDefaultUser() {
        return createUser("test@example.com", "testuser", "테스트유저");
    }

    public static User createSecondUser() {
        return createUser("test2@example.com", "testuser2", "테스트유저2");
    }

    public static User createUserWithTrack(DevCourseTrack track, String batch) {
        return User.builder()
                .email("user@track.com")
                .nickname("trackuser")
                .name("트랙유저")
                .role(Role.STUDENT)
                .providerType(ProviderType.LOCAL)
                .devcourseName(track)
                .devcourseBatch(batch)
                .imageUrl("https://example.com/profile.jpg")
                .build();
    }

    // === Post 관련 픽스처 ===
    public static Post createPost(User user, String title, String content, String tags) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MATCH)
                .title(title)
                .content(content)
                .tags(tags)
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    public static Post createDefaultPost() {
        return createPost(createDefaultUser(), "커피챗 모집합니다", "프론트엔드 개발 관련 커피챗 구합니다", "React,JavaScript");
    }

    public static Post createPostWithUser(User user) {
        return createPost(user, "멘토링 모집", "백엔드 개발 멘토링 해드립니다", "Spring,Java");
    }

    // === MatchingPost 관련 픽스처 ===
    public static MatchingPost createMatchingPost(Post post, MatchingType matchingType, MatchingStatus status, String expertiseArea) {
        return MatchingPost.builder()
                .post(post)
                .matchingType(matchingType)
                .status(status)
                .expertiseAreas(expertiseArea)
                .build();
    }

    public static MatchingPost createActiveCoffeeChatPost() {
        Post post = createDefaultPost();
        return createMatchingPost(post, MatchingType.COFFEE_CHAT, MatchingStatus.OPEN, "Frontend Development");
    }

    public static MatchingPost createActiveMentoringPost() {
        Post post = createPostWithUser(createSecondUser());
        return createMatchingPost(post, MatchingType.MENTORING, MatchingStatus.OPEN, "Backend Development");
    }

    public static MatchingPost createClosedMentoringPost() {
        Post post = createPostWithUser(createDefaultUser());
        return createMatchingPost(post, MatchingType.MENTORING, MatchingStatus.MATCHED, "Full Stack Development");
    }

    public static List<MatchingPost> createMatchingPostList() {
        return Arrays.asList(
                createActiveCoffeeChatPost(),
                createActiveMentoringPost(),
                createClosedMentoringPost()
        );
    }

    // === DTO 픽스처 ===
    public static MatchPostRequestDTO createMatchPostRequest() {
        return new MatchPostRequestDTO(
                "새로운 커피챗 모집",
                "React 개발에 대해 이야기 나누고 싶습니다",
                "React,Frontend",
                MatchingType.COFFEE_CHAT,
                MatchingStatus.OPEN,
                "Frontend Development"
        );
    }

    public static MatchPostRequestDTO createMentoringRequest() {
        return new MatchPostRequestDTO(
                "Spring Boot 멘토링",
                "Spring Boot 개발 멘토링 해드립니다",
                "Spring,Backend",
                MatchingType.MENTORING,
                MatchingStatus.OPEN,
                "Backend Development"
        );
    }

    public static MatchPostRequestDTO createInvalidRequest() {
        return new MatchPostRequestDTO(
                null, // 유효하지 않은 제목
                null, // 유효하지 않은 내용
                "tags",
                null, // 유효하지 않은 매칭 타입
                null, // 유효하지 않은 상태
                "expertise"
        );
    }

    public static MatchPostResponseDTO createMatchPostResponse() {
        return MatchPostResponseDTO.builder()
                .postId(1L)
                .nickname("testuser")
                .devcourseName(DevCourseTrack.AI_BACKEND)
                .devcourseBatch("1기")
                .userProfileImg("https://example.com/profile.jpg")
                .title("커피챗 모집합니다")
                .content("프론트엔드 개발 관련 커피챗 구합니다")
                .tags("React,JavaScript")
                .viewCount(10)
                .likeCount(3)
                .matchingType(MatchingType.COFFEE_CHAT)
                .status(MatchingStatus.OPEN)
                .expertiseArea("Frontend Development")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasImages(false)
                .hasLiked(false)
                .hasBookmarked(false)
                .build();
    }

    public static List<MatchPostResponseDTO> createMatchPostResponseList() {
        MatchPostResponseDTO dto1 = createMatchPostResponse();

        MatchPostResponseDTO dto2 = MatchPostResponseDTO.builder()
                .postId(2L)
                .nickname("testuser2")
                .devcourseName(DevCourseTrack.AI_BACKEND)
                .devcourseBatch("1기")
                .userProfileImg("https://example.com/profile2.jpg")
                .title("멘토링 모집")
                .content("백엔드 개발 멘토링 해드립니다")
                .tags("Spring,Java")
                .viewCount(5)
                .likeCount(1)
                .matchingType(MatchingType.MENTORING)
                .status(MatchingStatus.OPEN)
                .expertiseArea("Backend Development")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasImages(true)
                .hasLiked(true)
                .hasBookmarked(false)
                .build();

        return Arrays.asList(dto1, dto2);
    }

    // === 테스트용 인증 토큰 ===
    public static String createValidToken() {
        return "valid-jwt-token-for-test";
    }

    public static String createInvalidToken() {
        return "invalid-jwt-token";
    }

    public static String createExpiredToken() {
        return "expired-jwt-token";
    }

    // === 페이징 관련 픽스처 ===
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final String DEFAULT_SORT = "createdAt";
    public static final String DEFAULT_DIRECTION = "DESC";

    // === 테스트 상수 ===
    public static final Long VALID_POST_ID = 1L;
    public static final Long INVALID_POST_ID = 999L;
    public static final Long VALID_USER_ID = 1L;
    public static final Long INVALID_USER_ID = 999L;

    // === 검색 조건 픽스처 ===
    public static String createSearchKeyword() {
        return "커피챗";
    }

    public static String createTagKeyword() {
        return "React";
    }

    public static MatchingType[] getAllMatchingTypes() {
        return MatchingType.values();
    }

    public static MatchingStatus[] getAllMatchingStatuses() {
        return MatchingStatus.values();
    }
}
package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingType;
import kr.co.amateurs.server.domain.entity.user.User;


public class MatchTestFixture {
    public static MatchPostRequestDTO createMatchPostRequestDTO() {
        return new MatchPostRequestDTO(
                "Spring 멘토링 모집",
                "Spring 멘토링 해주실 분 구합니다.",
                "Spring",
                MatchingType.MENTORING,
                MatchingStatus.OPEN,
                "백엔드"
        );
    }

    public static Post createCoffeeChatPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MATCH)
                .title("커피챗 참가자 모집")
                .content("커피챗 진행할 멘티를 모집합니다.")
                .tags("커피챗,멘토링")
                .build();
    }

    public static Post createMentoringPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.MATCH)
                .title("멘토링 신청")
                .content("백엔드 멘토링 받고 싶어요.")
                .tags("멘토링,백엔드")
                .build();
    }

    public static MatchingPost createCoffeeChatMatchingPost(Post post) {
        return MatchingPost.builder()
                .post(post)
                .matchingType(MatchingType.COFFEE_CHAT)
                .status(MatchingStatus.OPEN)
                .expertiseAreas("백엔드")
                .build();
    }

    public static MatchingPost createMentoringMatchingPost(Post post) {
        return MatchingPost.builder()
                .post(post)
                .matchingType(MatchingType.MENTORING)
                .status(MatchingStatus.OPEN)
                .expertiseAreas("프론트엔드")
                .build();
    }
}
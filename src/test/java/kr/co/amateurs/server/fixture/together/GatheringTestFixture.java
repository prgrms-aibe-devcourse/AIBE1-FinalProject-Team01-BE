package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.user.User;

import static kr.co.amateurs.server.domain.entity.post.Post.convertTagToList;


public class GatheringTestFixture {
    public static GatheringPostRequestDTO createGatheringRequestDTO() {
        return new GatheringPostRequestDTO(
                "팀원 모집합니다",
                "React 스터디 팀원을 모집합니다.",
                convertTagToList("React"),
                GatheringType.STUDY,
                GatheringStatus.RECRUITING,
                4,
                "온라인",
                "3개월",
                "주 2회"
        );
    }

    public static Post createStudyPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.GATHER)
                .title("알고리즘 스터디 모집")
                .content("매주 화요일 저녁에 알고리즘 스터디를 진행합니다.")
                .tags("알고리즘,스터디,코딩테스트")
                .build();
    }

    public static Post createProjectPost(User user) {
        return Post.builder()
                .user(user)
                .boardType(BoardType.GATHER)
                .title("웹 프로젝트 팀원 모집")
                .content("React와 Spring Boot를 사용한 웹 프로젝트를 함께 진행할 팀원을 모집합니다.")
                .tags("React,Spring Boot,프로젝트")
                .build();
    }

    public static GatheringPost createStudyGatheringPost(Post post) {
        return GatheringPost.builder()
                .post(post)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(5)
                .place("강남역 스터디카페")
                .period("2025-07-01 ~ 2025-09-30")
                .schedule("매주 화요일 19:00-21:00")
                .build();
    }

    public static GatheringPost createProjectGatheringPost(Post post) {
        return GatheringPost.builder()
                .post(post)
                .gatheringType(GatheringType.SIDE_PROJECT)
                .status(GatheringStatus.RECRUITING)
                .headCount(4)
                .place("온라인")
                .period("2025-07-15 ~ 2025-10-15")
                .schedule("매주 토요일 14:00-18:00")
                .build();
    }
}

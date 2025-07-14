package kr.co.amateurs.server.fixture.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PopularPostRequestFixture {
    public static PopularPostRequest 생성(
            Long postId, int view, int like, int comment, double score, LocalDate date
    ) {
        return new PopularPostRequest(
                postId,
                view,
                like,
                comment,
                score,
                date,
                "테스터",
                DevCourseTrack.BACKEND,
                LocalDateTime.now(),
                "테스트 제목",
                BoardType.FREE,
                1L,
                null,
                null
        );
    }
} 
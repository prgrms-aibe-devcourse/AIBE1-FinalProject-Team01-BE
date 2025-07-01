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
    public static MatchPostRequestDTO getMatchPostRequestDTO() {
        return new MatchPostRequestDTO(
                "Spring 멘토링 모집",
                "Spring 멘토링 해주실 분 구합니다.",
                "Spring",
                MatchingType.MENTORING,
                MatchingStatus.OPEN,
                "백엔드"
        );
    }
}
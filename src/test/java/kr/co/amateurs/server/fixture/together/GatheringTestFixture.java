package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public class GatheringTestFixture {
    public static GatheringPostRequestDTO createGatheringRequestDTO() {
        return new GatheringPostRequestDTO(
                "팀원 모집합니다",
                "React 스터디 팀원을 모집합니다.",
                "React",
                GatheringType.STUDY,
                GatheringStatus.RECRUITING,
                4,
                "온라인",
                "3개월",
                "주 2회"
        );
    }
}

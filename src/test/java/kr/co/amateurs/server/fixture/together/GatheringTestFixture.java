package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;

public class GatheringTestFixture {
    public static String fakeStudentToken() {
        return "fake-student-token";
    }

    public static String fakeAdminToken() {
        return "fake-admin-token";
    }

    public static GatheringPostRequestDTO createValidRequestDTO() {
        return new GatheringPostRequestDTO(
                "팀원 모집합니다",
                "React 프로젝트 함께할 팀원을 모집합니다.",
                "React",
                GatheringType.SIDE_PROJECT,
                GatheringStatus.RECRUITING,
                4,
                "온라인",
                "3개월",
                "주 2회 화/목 오후 7시"
        );
    }
}

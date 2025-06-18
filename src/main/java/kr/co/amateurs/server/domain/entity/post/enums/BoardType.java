package kr.co.amateurs.server.domain.entity.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardType {
    FREE("자유게시판"),
    QNA("질문/토론"),
    MARKET("장터"),
    RETROSPECT("회고"),
    GATHER("팀원구하기"),
    MATCH("매칭"),
    PROJECT_HUB("프로젝트 허브"),
    INFO("정보게시판"),
    REVIEW("데브코스후기");

    private final String description;
}

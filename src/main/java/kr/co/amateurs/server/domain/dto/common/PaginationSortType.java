package kr.co.amateurs.server.domain.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaginationSortType {
    ID("id"),
    //  POST_AUTHOR("user.id") 연관관계를 사용한 정렬 시 예시코드
    EMPTY(null);

    public final String field;
}

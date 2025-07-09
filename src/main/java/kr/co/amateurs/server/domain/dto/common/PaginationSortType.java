package kr.co.amateurs.server.domain.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaginationSortType {
    ID("id"),
    DM_SENT_AT("sentAt"),
    EMPTY(null),
    LATEST("createdAt"),
    POPULAR("likeCount"),
    MOST_VIEW("viewCount"),

    POST_LATEST("post.createdAt"),
    POST_POPULAR("post.likeCount"),
    POST_MOST_VIEW("post.viewCount");

    public final String field;
}

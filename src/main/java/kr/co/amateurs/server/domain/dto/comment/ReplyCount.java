package kr.co.amateurs.server.domain.dto.comment;

public interface ReplyCount {
    Long getParentCommentId();
    Long getCount();
}

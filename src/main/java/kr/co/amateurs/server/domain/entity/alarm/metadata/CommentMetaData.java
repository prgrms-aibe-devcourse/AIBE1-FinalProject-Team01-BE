package kr.co.amateurs.server.domain.entity.alarm.metadata;

import kr.co.amateurs.server.domain.entity.post.enums.BoardType;

public record CommentMetaData(
        Long postId,
        BoardType boardType,
        Long commentId
) implements AlarmMetaData {
}

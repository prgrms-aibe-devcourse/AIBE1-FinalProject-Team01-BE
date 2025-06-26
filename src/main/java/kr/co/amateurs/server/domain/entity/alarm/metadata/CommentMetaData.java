package kr.co.amateurs.server.domain.entity.alarm.metadata;

public record CommentMetaData(
        Long postId,
        Long commentId
) implements AlarmMetaData {
}

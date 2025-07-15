package kr.co.amateurs.server.domain.event;

import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;

public record VerificationEvent(
        Long verifyId,
        String imageUrl,
        String filename,
        User user,
        DevCourseTrack devcourseName,
        String devcourseBatch
) {
}

package kr.co.amateurs.server.domain.dto.user;

import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

public record UserModalInfoResponseDTO(
        Long userId,
        String nickname,
        String imageUrl,
        DevCourseTrack devCourseName,
        String devCourseBatch,
        boolean isFollowing
) {
}

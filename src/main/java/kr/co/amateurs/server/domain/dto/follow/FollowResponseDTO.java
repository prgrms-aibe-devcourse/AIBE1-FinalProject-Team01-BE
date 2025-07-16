package kr.co.amateurs.server.domain.dto.follow;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

public record FollowResponseDTO(
        @Schema(description = "팔로우 대상(또는 팔로워) 유저 ID", example = "1")
        Long userId,
        @Schema(description = "팔로우 대상(또는 팔로워) 유저 닉네임", example = "testNickName")
        String nickname,
        @Schema(description = "팔로우 대상(또는 팔로워) 유저 프로필 이미지", example = "example.com")
        String profileImg,
        DevCourseTrack devcourseTrack,
        String devcourseBatch,
        Integer postCount,
        Integer follwerCount,
        Integer followingCount
) {}

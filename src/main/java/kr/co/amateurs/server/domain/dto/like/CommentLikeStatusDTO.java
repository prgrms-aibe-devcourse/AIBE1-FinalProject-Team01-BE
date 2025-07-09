package kr.co.amateurs.server.domain.dto.like;

import java.util.List;

public record CommentLikeStatusDTO(Long commentId, Boolean isLiked) {}
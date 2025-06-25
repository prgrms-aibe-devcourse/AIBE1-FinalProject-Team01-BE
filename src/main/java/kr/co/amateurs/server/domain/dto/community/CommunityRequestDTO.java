package kr.co.amateurs.server.domain.dto.community;

import kr.co.amateurs.server.domain.dto.post.PostRequest;

public record CommunityRequestDTO(
   String title,
   String tags,
   String content
) implements PostRequest {
}

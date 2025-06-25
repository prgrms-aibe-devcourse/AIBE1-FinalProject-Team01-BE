package kr.co.amateurs.server.domain.dto.it;

import kr.co.amateurs.server.domain.dto.post.PostRequest;

public record ITRequestDTO (
   String title,
   String tags,
   String content
)implements PostRequest {
}

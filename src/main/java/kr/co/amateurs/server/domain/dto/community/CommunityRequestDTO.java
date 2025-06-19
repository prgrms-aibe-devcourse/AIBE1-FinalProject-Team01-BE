package kr.co.amateurs.server.domain.dto.community;

public record CommunityRequestDTO(
   String title,
   String tags,
   String content
) {
}

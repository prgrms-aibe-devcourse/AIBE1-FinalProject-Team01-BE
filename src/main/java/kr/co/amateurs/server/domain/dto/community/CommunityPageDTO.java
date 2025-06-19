package kr.co.amateurs.server.domain.dto.community;

import java.util.List;

public record CommunityPageDTO (
    List<CommunityResponseDTO> communityList,
    int currentPage,
    int pageSize,
    int totalPages
){}

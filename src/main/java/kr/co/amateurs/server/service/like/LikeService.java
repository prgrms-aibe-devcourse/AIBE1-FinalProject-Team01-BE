package kr.co.amateurs.server.service.like;


import kr.co.amateurs.server.domain.dto.like.LikeRequestDTO;
import kr.co.amateurs.server.domain.dto.like.LikeResponseDTO;
import kr.co.amateurs.server.repository.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public LikeResponseDTO addLikeToPost(Long postId, LikeRequestDTO likeRequestDTO) {
        LikeResponseDTO dto = new LikeResponseDTO();
        return dto;
    }

    public LikeResponseDTO addLikeToComment(Long commentId, LikeRequestDTO likeRequestDTO) {
        LikeResponseDTO dto = new LikeResponseDTO();
        return dto;

    }
}

package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.TogetherPaginationParam;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;

    public PageResponseDTO<GatheringPostResponseDTO> getGatheringPostList(TogetherPaginationParam paginationParam) {
//        Pageable pageable = paginationParam.toPageable();
        Page<GatheringPost> gpPage = gatheringRepository.findByKeyword(paginationParam.getKeyword(), paginationParam.toPageable());
//                switch (paginationParam.getField()) {
//            case LATEST -> gatheringRepository.findAllByKeyword(keyword, pageable);
//            case POPULAR -> gatheringRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
//            case MOST_VIEW -> gatheringRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
//            default -> gatheringRepository.findAllByKeyword(keyword, pageable);
//        };
        return convertPageToDTO(convertToDTO(gpPage));
    }

    public GatheringPostResponseDTO getGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = gp.getPost();
        return convertToDTO(gp, post);
    }


    @Transactional
    public GatheringPostResponseDTO createGatheringPost(CustomUserDetails currentUser, GatheringPostRequestDTO dto) {
        Post post = Post.builder()
                .user(currentUser.getUser())
                .boardType(BoardType.GATHER)
                .title(dto.title())
                .content(dto.content())
                .tags(dto.tags())
                .build();
        Post savedPost = postRepository.save(post);

        GatheringPost gp = GatheringPost.builder()
                .post(savedPost)
                .gatheringType(dto.gatheringType())
                .status(GatheringStatus.RECRUITING)
                .headCount(dto.headCount())
                .place(dto.place())
                .period(dto.period())
                .schedule(dto.schedule())
                .build();
        GatheringPost savedGp = gatheringRepository.save(gp);

        return convertToDTO(savedGp, savedPost);
    }


    @Transactional
    public void updateGatheringPost(CustomUserDetails currentUser, Long postId, GatheringPostRequestDTO dto) {
        GatheringPost gp = gatheringRepository.findByPostId(postId);
        if(gp == null) {
            throw new IllegalArgumentException("Gathering Post not found: " + postId);
        }
        Post post = gp.getPost();
        if(currentUser.getUser().getId().equals(post.getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
            post.update(updatePostDTO);
            gp.update(dto);
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You don't have permission to update this post");
        }

    }

    @Transactional
    public void deleteGatheringPost(CustomUserDetails currentUser, Long postId) {
        GatheringPost gp = gatheringRepository.findByPostId(postId);
        Post post = gp.getPost();
        if(currentUser.getUser().getId().equals(gp.getPost().getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            gatheringRepository.deleteById(gp.getId());
            postRepository.deleteById(post.getId());
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You don't have permission to delete this post");
        }
    }

}


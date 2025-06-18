package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.dto.community.CommunityPageDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final int SIZE = 8;
    
    // TODO CustomException 변경

    public CommunityPageDTO searchPosts(String keyword, int page, BoardType boardType, SortType sortType) {
        Pageable pageable = createPageable(page, sortType);

        Page<Post> communityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            communityPage = postRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            communityPage = postRepository.findByBoardType(boardType, pageable);
        }
        
        List<CommunityResponseDTO> communityList = communityPage.getContent().stream()
                .map((Post post) -> convertToResponseDTO(post, false))
                .collect(Collectors.toList());
        
        return new CommunityPageDTO(
                communityList,
                page,
                SIZE,
                communityPage.getTotalPages()
        );
    }

    public CommunityResponseDTO getPost(BoardType boardType, Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null || post.getBoardType() != boardType) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        // TODO 좋아요 확인 로직 필요 << 로그인 기능 이후 구현 예정
        boolean hasLiked = false;

        return convertToResponseDTO(post, hasLiked);
    }

    @Transactional
    public CommunityResponseDTO createPost(CommunityRequestDTO requestDTO, BoardType boardType) {
        // TODO 유저 불러오기
        User user = null;

        Post post = Post.from(requestDTO, user, boardType);

        postRepository.save(post);

        return convertToResponseDTO(post, false);
    }

    @Transactional
    public void updatePost(CommunityRequestDTO requestDTO, Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        
        // TODO 유저 검증 로직
        User user = null;
        if (post.getUser().getId() != user.getId()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        post.update(requestDTO);

        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        // TODO 유저 검증 로직
        User user = null;
        if (post.getUser().getId() != user.getId()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        // TODO soft delete 구현 시 변경 >> deletedAT??
        postRepository.delete(post);
    }

    private Pageable createPageable(int page, SortType sortType) {
        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case POPULAR -> Sort.by(Sort.Direction.DESC, "likeCount");
            case VIEW_COUNT -> Sort.by(Sort.Direction.DESC, "viewCount");
        };

        return PageRequest.of(page, SIZE, sort);
    }

    private CommunityResponseDTO convertToResponseDTO(Post post, boolean hasLiked) {
        String thumbnailImage = null;
        boolean hasImages = post.getPostImages() != null && !post.getPostImages().isEmpty();
        if (hasImages) {
            thumbnailImage = post.getPostImages().get(0).getImageUrl();
        }
        
        int commentCount = post.getComments().size();
        
        return CommunityResponseDTO.from(post, commentCount, thumbnailImage, hasImages, hasLiked);
    }


}

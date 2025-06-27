package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostService {
    private final PostRepository postRepository;
    private final BookmarkService bookmarkService;
    private final LikeService likeService;

    private final UserService userService;

    public PageResponseDTO<CommunityResponseDTO> searchPosts(BoardType boardType, PostPaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        String keyword = paginationParam.getKeyword();
        Page<Post> communityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            communityPage = postRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            communityPage = postRepository.findByBoardType(boardType, pageable);
        }

        return convertPageToDTO(communityPage.map(post -> CommunityResponseDTO.from(post, false, false)));
    }

    public Post findById(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }

    public CommunityResponseDTO getPost(Long postId) {
        User user = userService.getCurrentUser().orElse(null);

        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);

        boolean hasBookmarked = false;
        boolean hasLiked = false;
        if (user != null) {
            hasBookmarked = bookmarkService.checkHasBookmarked(postId);
            hasLiked = likeService.checkHasLiked(postId);
        }

        return CommunityResponseDTO.from(post, hasLiked, hasBookmarked);
    }

    @Transactional
    public CommunityResponseDTO createPost(CommunityRequestDTO requestDTO, BoardType boardType) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        Post post = Post.from(requestDTO, user, boardType);

        postRepository.save(post);

        return CommunityResponseDTO.from(post, false, false);
    }

    @Transactional
    public void updatePost(CommunityRequestDTO requestDTO, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);

        validatePost(post);

        post.update(requestDTO);

        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);

        validatePost(post);

        // TODO soft delete 구현 시 변경 >> deletedAT??
        postRepository.delete(post);
    }

    private void validatePost(Post post) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        if (!Objects.equals(post.getUser().getId(), user.getId())) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    // TODO - 사용자가 한 게시글을 여러번 조회할 경우 viewCount를 중복으로 올라가도록 할 지 한 명당 1회만 올라가게 할 지 정해야 함
    //         + 조회수 증가를 별도의 API로 할 지 다른 곳에다 붙일 지 정해야 함
    @Transactional
    public void increaseViewCount(Long postId) {
        postRepository.increaseViewCount(postId);
    }
}

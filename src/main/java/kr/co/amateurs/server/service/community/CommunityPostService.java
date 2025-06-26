package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.common.ErrorCode;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPostService {
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    private final UserService userService;

    public Page<CommunityResponseDTO> searchPosts(BoardType boardType, PostPaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        String keyword = paginationParam.getKeyword();
        Page<Post> communityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            communityPage = postRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            communityPage = postRepository.findByBoardType(boardType, pageable);
        }

        return communityPage.map(post -> CommunityResponseDTO.from(post, false, false));
    }

    public CommunityResponseDTO getPost(Long postId) {
        User user = userService.getCurrentUser().orElse(null);

        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);

        boolean hasBookmarked = false;
        boolean hasLiked = false;
        if (user != null) {
            hasBookmarked = checkHasBookmarked(postId, user);
            hasLiked = checkHasLiked(postId, user);
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

    private boolean checkHasLiked(Long postId, User user) {
        return likeRepository
                .findByPost_IdAndUser_Id(postId, user.getId())
                .isPresent();
    }

    private boolean checkHasBookmarked(Long postId, User user) {
        return bookmarkRepository
                .findByPost_IdAndUser_Id(postId, user.getId())
                .isPresent();
    }

    private void validatePost(Post post) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        if (!Objects.equals(post.getUser().getId(), user.getId())) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }
}

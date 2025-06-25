package kr.co.amateurs.server.service.it;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITService {
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    private final UserService userService;

    public Page<ITResponseDTO> searchPosts(String keyword, int page, BoardType boardType, SortType sortType, int pageSize) {
        // Community와 코드와 현재는 동일 이후에 뉴스가 나오면 뉴스 종류나 카테고리 필드가 추가 될 수도 있어서 서비스 레이어 생성

        Pageable pageable = createPageable(page, sortType, pageSize);

        Page<Post> itPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            itPage = postRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            itPage = postRepository.findByBoardType(boardType, pageable);
        }

        return itPage.map(post -> ITResponseDTO.from(post, false, false));
    }

    public ITResponseDTO getPost(Long postId) {
        User user = userService.getCurrentUser().orElse(null);

        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);

        boolean hasBookmarked = false;
        boolean hasLiked = false;
        if (user != null) {
            hasBookmarked = checkHasBookmarked(postId, user);
            hasLiked = checkHasLiked(postId, user);
        }

        return ITResponseDTO.from(post, hasLiked, hasBookmarked);
    }

    @Transactional
    public ITResponseDTO createPost(ITRequestDTO requestDTO, BoardType boardType) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        Post post = Post.from(requestDTO, user, boardType);

        postRepository.save(post);

        return ITResponseDTO.from(post, false, false);
    }

    @Transactional
    public void updatePost(ITRequestDTO requestDTO, Long postId) {
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

    private Pageable createPageable(int page, SortType sortType, int pageSize) {
        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case POPULAR -> Sort.by(Sort.Direction.DESC, "likeCount");
            case VIEW_COUNT -> Sort.by(Sort.Direction.DESC, "viewCount");
        };

        return PageRequest.of(page, pageSize, sort);
    }
}

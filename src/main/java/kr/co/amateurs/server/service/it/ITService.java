package kr.co.amateurs.server.service.it;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.ITPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.it.ITRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class ITService {
    private final ITRepository itRepository;
    private final PostRepository postRepository;

    private final BookmarkService bookmarkService;
    private final LikeService likeService;

    private final UserService userService;

    public PageResponseDTO<ITResponseDTO> searchPosts(BoardType boardType, PostPaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        String keyword = paginationParam.getKeyword();
        Page<ITPost> itPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            itPage = itRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            itPage = itRepository.findByBoardType(boardType, pageable);
        }

        return convertPageToDTO(itPage.map(itPost -> ITResponseDTO.from(itPost, false, false)));
    }

    public ITResponseDTO getPost(Long itId) {
        Optional<User> user = userService.getCurrentUser();

        ITPost itPost = findById(itId);

        boolean hasBookmarked = false;
        boolean hasLiked = false;
        if (user.isPresent()) {
            hasBookmarked = bookmarkService.checkHasBookmarked(itPost.getPost().getId(), user.get().getId());
            hasLiked = likeService.checkHasLiked(itPost.getPost().getId(), user.get().getId());
        }

        return ITResponseDTO.from(itPost, hasLiked, hasBookmarked);
    }

    @Transactional
    public ITResponseDTO createPost(ITRequestDTO requestDTO, BoardType boardType) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        Post post = Post.from(requestDTO, user, boardType);

        Post savedPost = postRepository.save(post);

        ITPost itPost = ITPost.from(savedPost);
        ITPost savedITPost = itRepository.save(itPost);


        return ITResponseDTO.from(savedITPost, false, false);
    }

    @Transactional
    public void updatePost(ITRequestDTO requestDTO, Long postId) {
        ITPost itPost = findById(postId);

        Post post = itPost.getPost();
        validatePost(post);

        post.update(requestDTO);
    }

    @Transactional
    public void deletePost(Long postId) {
        ITPost itPost = findById(postId);

        Post post = itPost.getPost();
        validatePost(post);

        itRepository.delete(itPost);
    }

    private void validatePost(Post post) {
        User user = userService.getCurrentUser().orElseThrow(ErrorCode.USER_NOT_FOUND);

        if (!canEditOrDelete(post, user)) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    private boolean canEditOrDelete(Post post, User user) {
        return Objects.equals(post.getUser().getId(), user.getId()) || user.getRole() == Role.ADMIN;
    }

    private ITPost findById(Long itId) {
        return itRepository.findById(itId)
                .orElseThrow(ErrorCode.NOT_FOUND);
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

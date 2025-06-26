package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.TogetherPaginationParam;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;

    private final UserService userService;

    public PageResponseDTO<GatheringPostResponseDTO> getGatheringPostList(TogetherPaginationParam paginationParam) {
        String keyword = paginationParam.getKeyword();
        Pageable pageable = paginationParam.toPageable();
        Page<GatheringPost> gpPage = switch (paginationParam.getField()) {
            case LATEST -> gatheringRepository.findAllByKeyword(keyword, pageable);
            case POPULAR -> gatheringRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
            case MOST_VIEW -> gatheringRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
            default -> gatheringRepository.findAllByKeyword(keyword, pageable);
        };
        Page<GatheringPostResponseDTO> response = gpPage.map(gp->convertToDTO(gp, gp.getPost(), checkHasLiked(gp.getPost().getId()), checkHasBookmarked(gp.getPost().getId())));
        return convertPageToDTO(response);
    }


    public GatheringPostResponseDTO getGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findByPostId(id);
        if(gp == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = gp.getPost();
        return convertToDTO(gp, post, checkHasLiked(post.getId()), checkHasBookmarked(post.getId()));
    }


    @Transactional
    public GatheringPostResponseDTO createGatheringPost(GatheringPostRequestDTO dto) {
        User currentUser = getCurrentUser();
        Post post = Post.builder()
                .user(currentUser)
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

        return convertToDTO(savedGp, savedPost, checkHasLiked(savedPost.getId()), checkHasBookmarked(savedPost.getId()));
    }


    @Transactional
    public void updateGatheringPost(Long postId, GatheringPostRequestDTO dto) {
        GatheringPost gp = gatheringRepository.findByPostId(postId);
        if(gp == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = gp.getPost();
        validateUser(post.getUser().getId());
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
        post.update(updatePostDTO);
        gp.update(dto);
    }

    @Transactional
    public void deleteGatheringPost(Long postId) {
        GatheringPost gp = gatheringRepository.findByPostId(postId);
        if(gp == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = gp.getPost();
        validateUser(post.getUser().getId());
        gatheringRepository.deleteById(gp.getId());
        postRepository.deleteById(post.getId());
    }

    private User getCurrentUser() {
        Optional<User> user = Objects.requireNonNull(userService).getCurrentUser();
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user.get();
    }
    private void validateUser(Long userId) {
        User currentUser = getCurrentUser();
        Long currentId = currentUser.getId();
        Role currentRole = currentUser.getRole();
        if (!currentId.equals(userId) && currentRole != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "해당 게시글에 접근할 수 없습니다.");
        }
    }
    private boolean checkHasLiked(Long postId) {
        User user = getCurrentUser();
        return likeRepository
                .findByPost_IdAndUser_Id(postId, user.getId())
                .isPresent();
    }
    private boolean checkHasBookmarked(Long postId) {
        User user = getCurrentUser();
        return bookmarkRepository
                .findByPost_IdAndUser_Id(postId, user.getId())
                .isPresent();
    }
}


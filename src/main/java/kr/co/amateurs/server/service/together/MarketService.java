package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.TogetherPaginationParam;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
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
import static kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO.convertToDTO;


@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketRepository marketRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;


    public PageResponseDTO<MarketPostResponseDTO> getMarketPostList(TogetherPaginationParam paginationParam) {
        String keyword = paginationParam.getKeyword();
        Pageable pageable = paginationParam.toPageable();
        Page<MarketItem> miPage = switch (paginationParam.getField()) {
            case LATEST -> marketRepository.findAllByKeyword(keyword, pageable);
            case POPULAR -> marketRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
            case MOST_VIEW -> marketRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
            default -> marketRepository.findAllByKeyword(keyword, pageable);
        };
        Page<MarketPostResponseDTO> response = miPage.map(mi-> convertToDTO(mi, mi.getPost(), likeService.checkHasLiked(mi.getPost().getId()), bookmarkService.checkHasBookmarked(mi.getPost().getId())));
        return convertPageToDTO(response);
    }


    public MarketPostResponseDTO getMarketPost(Long id) {
        MarketItem mi = marketRepository.findByPostId(id);
        if(mi==null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = mi.getPost();
        return convertToDTO(mi, post, likeService.checkHasLiked(post.getId()), bookmarkService.checkHasBookmarked(post.getId()));
    }


    @Transactional
    public MarketPostResponseDTO createMarketPost(MarketPostRequestDTO dto) {
        User currentUser = getCurrentUser();
        Post post = Post.builder()
                .user(currentUser)
                .boardType(BoardType.MARKET)
                .title(dto.title())
                .content(dto.content())
                .tags(dto.tags())
                .build();
        Post savedPost = postRepository.save(post);

        MarketItem mi = MarketItem.builder()
                .post(savedPost)
                .status(MarketStatus.SELLING)
                .price(dto.price())
                .place(dto.place())
                .build();
        MarketItem savedMp = marketRepository.save(mi);

        return convertToDTO(savedMp, savedPost, likeService.checkHasLiked(savedPost.getId()), bookmarkService.checkHasBookmarked(savedPost.getId()));
    }

    @Transactional
    public void updateMarketPost(Long postId, MarketPostRequestDTO dto) {
        MarketItem mi = marketRepository.findByPostId(postId);
        if (mi == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = mi.getPost();
        validateUser(post.getUser().getId());
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
        post.update(updatePostDTO);
        mi.update(dto);

    }

    @Transactional
    public void deleteMarketPost(Long marketId) {
        MarketItem mi = marketRepository.findByPostId(marketId);
        if (mi == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        Post post = mi.getPost();
        validateUser(post.getUser().getId());
        marketRepository.deleteById(mi.getId());
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
}

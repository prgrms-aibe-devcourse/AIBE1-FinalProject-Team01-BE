package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.file.FileService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final FileService fileService;


    public PageResponseDTO<MarketPostResponseDTO> getMarketPostList(PostPaginationParam paginationParam) {
        Page<MarketItem> miPage = marketRepository.findAllByKeyword(paginationParam.getKeyword(), paginationParam.toPageable());
        Page<MarketPostResponseDTO> response = miPage.map(mi-> convertToDTO(mi, mi.getPost(), likeService.checkHasLiked(mi.getPost().getId()), bookmarkService.checkHasBookmarked(mi.getPost().getId())));
        return convertPageToDTO(response);
    }


    public MarketPostResponseDTO getMarketPost(Long id) {
        MarketItem mi = marketRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
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

        List<String> imgUrls = fileService.extractImageUrls(dto.content());
        fileService.savePostImage(savedPost, imgUrls);

        return convertToDTO(savedMp, savedPost, likeService.checkHasLiked(savedPost.getId()), bookmarkService.checkHasBookmarked(savedPost.getId()));
    }

    @Transactional
    public void updateMarketPost(Long id, MarketPostRequestDTO dto) {
        MarketItem mi = marketRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mi.getPost();
        validateUser(post.getUser().getId());
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
        post.update(updatePostDTO);
        mi.update(dto);

    }

    @Transactional
    public void deleteMarketPost(Long marketId) {
        MarketItem mi = marketRepository.findById(marketId).orElseThrow(ErrorCode.POST_NOT_FOUND);
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

package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.post.PostViewedEvent;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.file.FileService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO.convertToDTO;
import static kr.co.amateurs.server.domain.entity.post.Post.convertListToTag;


@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {
    private final MarketRepository marketRepository;
    private final PostRepository postRepository;
    private final PostStatisticsRepository postStatisticsRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    private final FileService fileService;
    private final PostEmbeddingService postEmbeddingService;

    private final ApplicationEventPublisher eventPublisher;


    public PageResponseDTO<MarketPostResponseDTO> getMarketPostList(PostPaginationParam paginationParam) {
        String keyword = paginationParam.getKeyword();
        Page<MarketPostResponseDTO> mpPage;
        if (paginationParam.getField() == PaginationSortType.POST_MOST_VIEW) {
            Pageable pageable = PageRequest.of(paginationParam.getPage(), paginationParam.getSize());
            mpPage = marketRepository.findDTOByContentOrderByViewCount(keyword, pageable);
        }else{
            Pageable pageable = paginationParam.toPageable();
            mpPage = marketRepository.findDTOByContent(keyword, pageable);
        }

        Page<MarketPostResponseDTO> processedPage = mpPage.map(MarketPostResponseDTO::applyBlindFilter);

        return convertPageToDTO(processedPage);
    }


    public MarketPostResponseDTO getMarketPost(Long id, String ipAddress) {
        User user = userService.getCurrentLoginUser();

        MarketPostResponseDTO mp = marketRepository.findDTOByIdAndUserId(id, user.getId())
                .orElseThrow(ErrorCode.POST_NOT_FOUND);

        eventPublisher.publishEvent(new PostViewedEvent(mp.postId(), ipAddress));
        return mp.applyBlindFilter();
    }


    @Transactional
    public MarketPostResponseDTO createMarketPost(MarketPostRequestDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        Post post = Post.builder()
                .user(currentUser)
                .boardType(BoardType.MARKET)
                .title(dto.title())
                .content(dto.content())
                .tags(convertListToTag(dto.tags()))
                .build();
        Post savedPost = postRepository.save(post);

        MarketItem mi = MarketItem.builder()
                .post(savedPost)
                .status(MarketStatus.SELLING)
                .price(dto.price())
                .place(dto.place())
                .build();
        MarketItem savedMp = marketRepository.save(mi);

        PostStatistics postStatistics = PostStatistics.from(savedPost);
        PostStatistics savedPs = postStatisticsRepository.save(postStatistics);


        List<String> imgUrls = fileService.extractImageUrls(dto.content());
        fileService.savePostImage(savedPost, imgUrls);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.createPostEmbeddings(savedPost);
            } catch (Exception e) {
                log.warn("커뮤니티 게시글 임베딩 생성 실패: postId={}", savedPost.getId(), e);
            }
        });

        return convertToDTO(savedMp, savedPost);
    }

    @Transactional
    public void updateMarketPost(Long id, MarketPostRequestDTO dto) {
        MarketItem mi = marketRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mi.getPost();
        validateUser(post);
        if(post.getIsBlinded()){
            throw ErrorCode.IS_BLINDED_POST.get();
        }
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.tags(), dto.content());
        post.update(updatePostDTO);
        mi.update(dto);

    }

    @Transactional
    public void deleteMarketPost(Long marketId) {
        MarketItem mi = marketRepository.findById(marketId).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mi.getPost();
        validateUser(post);

        postStatisticsRepository.deleteById(post.getId());
        bookmarkRepository.deleteByPost_Id(post.getId());
        likeRepository.deleteByPost_Id(post.getId());
        reportRepository.deleteByPost_Id(post.getId());
        commentRepository.deleteByPostId(post.getId());
        fileService.deletePostImage(post);
        postRepository.delete(post);
    }

    private void validateUser(Post post) {
        User currentUser = userService.getCurrentLoginUser();

        if (!canEditOrDelete(post, currentUser)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }


    private boolean canEditOrDelete(Post post, User user) {
        return Objects.equals(post.getUser().getId(), user.getId()) || user.getRole() == Role.ADMIN;
    }
}

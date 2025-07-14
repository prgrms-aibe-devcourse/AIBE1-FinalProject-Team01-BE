package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.post.PostViewedEvent;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.file.FileService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.function.Function;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO.convertToDTO;
import static kr.co.amateurs.server.domain.entity.post.Post.convertListToTag;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;
    private final PostStatisticsRepository postStatisticsRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    private final UserService userService;
    private final FileService fileService;

    private final PostEmbeddingService postEmbeddingService;

    private final ApplicationEventPublisher eventPublisher;

    public PageResponseDTO<GatheringPostResponseDTO> getGatheringPostList(PostPaginationParam paginationParam) {
        Page<GatheringPost> gpPage = gatheringRepository.findAllByKeyword(paginationParam.getKeyword(), paginationParam.toPageable());

        List<Long> postIds = gpPage.getContent().stream()
                .map(gp -> gp.getPost().getId())
                .toList();

        List<PostStatistics> statisticsList = postStatisticsRepository.findByPostIdIn(postIds);
        Map<Long, PostStatistics> statisticsMap = statisticsList.stream()
                .collect(Collectors.toMap(PostStatistics::getPostId, Function.identity()));

        Page<GatheringPostResponseDTO> response = gpPage.map(gp ->
            convertToDTO(gp, gp.getPost(), statisticsMap.get(gp.getPost().getId()), false, false, bookmarkService.countBookmark(gp.getPost()))
        );

        return convertPageToDTO(response);
    }


    public GatheringPostResponseDTO getGatheringPost(Long id, String ipAddress) {
        User user = userService.getCurrentLoginUser();

        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();

        PostStatistics postStatistics = postStatisticsRepository.findById(post.getId()).orElseThrow(ErrorCode.POST_NOT_FOUND);

        eventPublisher.publishEvent(new PostViewedEvent(post.getId(), ipAddress));
        return convertToDTO(gp, post,postStatistics, likeService.checkHasLiked(post.getId(), user.getId()), bookmarkService.checkHasBookmarked(post.getId(), user.getId()), bookmarkService.countBookmark(post));
    }


    @Transactional
    public GatheringPostResponseDTO createGatheringPost(GatheringPostRequestDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
        Post post = Post.builder()
                .user(currentUser)
                .boardType(BoardType.GATHER)
                .title(dto.title())
                .content(dto.content())
                .tags(convertListToTag(dto.tags()))
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

        PostStatistics postStatistics = PostStatistics.from(savedPost);
        PostStatistics savedps = postStatisticsRepository.save(postStatistics);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.createPostEmbeddings(savedPost);
            } catch (Exception e) {
                log.warn("커뮤니티 게시글 임베딩 생성 실패: postId={}", savedPost.getId(), e);
            }
        });

        List<String> imgUrls = fileService.extractImageUrls(dto.content());
        fileService.savePostImage(savedPost, imgUrls);

        return convertToDTO(savedGp, savedPost,savedps, false, false, 0);
    }


    @Transactional
    public void updateGatheringPost(Long id, GatheringPostRequestDTO dto) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();
        validateUser(post);
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.tags(), dto.content());
        post.update(updatePostDTO);
        gp.update(dto);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.updatePostEmbedding(post);
            } catch (Exception e) {
                log.warn("게시글 임베딩 업데이트 실패: postId={}", post.getId(), e);
            }
        });
    }

    @Transactional
    public void deleteGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();
        validateUser(post);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.deletePostEmbedding(post.getId());
            } catch (Exception e) {
                log.warn("게시글 임베딩 삭제 실패: postId={}", post.getId(), e);
            }
        });

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


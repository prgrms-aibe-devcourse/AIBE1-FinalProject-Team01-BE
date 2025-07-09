package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.post.PostViewedEvent;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.file.PostImageRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.file.FileService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO.convertToDTO;
import static kr.co.amateurs.server.domain.entity.post.Post.convertListToTag;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostStatisticsRepository postStatisticsRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    private final FileService fileService;
    private final PostEmbeddingService postEmbeddingService;

    private final ApplicationEventPublisher eventPublisher;

    public PageResponseDTO<MatchPostResponseDTO> getMatchPostList(PostPaginationParam paginationParam) {
        Page<MatchingPost> mpPage = matchRepository.findAllByKeyword(paginationParam.getKeyword(), paginationParam.toPageable());

        List<Long> postIds = mpPage.getContent().stream()
                .map(mp -> mp.getPost().getId())
                .toList();

        List<PostStatistics> statisticsList = postStatisticsRepository.findByPostIdIn(postIds);
        Map<Long, PostStatistics> statisticsMap = statisticsList.stream()
                .collect(Collectors.toMap(PostStatistics::getPostId, Function.identity()));

        Page<MatchPostResponseDTO> response = mpPage.map(mp ->
                MatchPostResponseDTO.convertToDTO(mp, mp.getPost(), statisticsMap.get(mp.getPost().getId()), false, false)
        );

        return convertPageToDTO(response);
    }


    public MatchPostResponseDTO getMatchPost(Long id, String ipAddress) {
        User user = userService.getCurrentLoginUser();
        MatchingPost mp = matchRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mp.getPost();

        PostStatistics postStatistics = postStatisticsRepository.findById(mp.getPost().getId()).orElseThrow(ErrorCode.NOT_FOUND);

        eventPublisher.publishEvent(new PostViewedEvent(post.getId(), ipAddress));
        return convertToDTO(mp, post, postStatistics, likeService.checkHasLiked(post.getId(), user.getId()), bookmarkService.checkHasBookmarked(post.getId(), user.getId()));
    }


    @Transactional
    public MatchPostResponseDTO createMatchPost(MatchPostRequestDTO dto) {
        User currentUser = userService.getCurrentLoginUser();

        Post post = Post.builder()
                .user(currentUser)
                .boardType(BoardType.MATCH)
                .title(dto.title())
                .content(dto.content())
                .tags(convertListToTag(dto.tags()))
                .build();
        Post savedPost = postRepository.save(post);

        MatchingPost mp = MatchingPost.builder()
                .post(savedPost)
                .matchingType(dto.matchingType())
                .status(MatchingStatus.OPEN)
                .expertiseAreas(dto.expertiseArea())
                .build();
        MatchingPost savedMp = matchRepository.save(mp);

        PostStatistics postStatistics = PostStatistics.from(savedPost);
        PostStatistics savedPs = postStatisticsRepository.save(postStatistics);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.createPostEmbeddings(savedPost);
            } catch (Exception e) {
                log.warn("커뮤니티 게시글 임베딩 생성 실패: postId={}", savedPost.getId(), e);
            }
        });

        List<String> imgUrls = fileService.extractImageUrls(dto.content());
        fileService.savePostImage(savedPost, imgUrls);

        return convertToDTO(savedMp, savedPost, savedPs, false, false);
    }

    @Transactional
    public void updateMatchPost(Long id, MatchPostRequestDTO dto) {
        MatchingPost mp = matchRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mp.getPost();
        validateUser(post);
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.tags(), dto.content());
        mp.update(dto);
        post.update(updatePostDTO);
    }

    @Transactional
    public void deleteMatchPost(Long id) {
        MatchingPost mp = matchRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = mp.getPost();
        validateUser(post);

        fileService.deletePostImage(post);
        postStatisticsRepository.deleteById(post.getId());
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

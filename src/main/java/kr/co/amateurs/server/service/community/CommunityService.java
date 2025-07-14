package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.post.PostViewedEvent;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.comment.CommentRepository;
import kr.co.amateurs.server.repository.community.CommunityRepository;
import kr.co.amateurs.server.repository.file.PostImageRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.report.ReportRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final PostStatisticsRepository postStatisticsRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    private final UserService userService;
    private final PostEmbeddingService postEmbeddingService;
    private final FileService fileService;

    private final ApplicationEventPublisher eventPublisher;

    public PageResponseDTO<CommunityResponseDTO> searchPosts(BoardType boardType, PostPaginationParam paginationParam) {
        String keyword = paginationParam.getKeyword();
        Page<CommunityResponseDTO> communityPage;

        if (paginationParam.getField() == PaginationSortType.POST_MOST_VIEW){
            Pageable pageable = PageRequest.of(paginationParam.getPage(), paginationParam.getSize());
            if (keyword != null && !keyword.trim().isEmpty()) {
                communityPage = communityRepository.findDTOByContentAndBoardTypeOrderByViewCount(keyword.trim(), boardType, pageable);
            }
            else{
                communityPage = communityRepository.findDTOByBoardTypeOrderByViewCount(boardType, pageable);
            }
        }else{
            Pageable pageable = paginationParam.toPageable();
            if (keyword != null && !keyword.trim().isEmpty()) {
                communityPage = communityRepository.findDTOByContentAndBoardType(keyword.trim(), boardType, pageable);
            } else {
                communityPage = communityRepository.findDTOByBoardType(boardType, pageable);
            }
        }

        Page<CommunityResponseDTO> processedPage = communityPage.map(CommunityResponseDTO::applyBlindFilter);

        return convertPageToDTO(processedPage);
 }

    public CommunityResponseDTO getPost(Long communityId, String ipAddress) {
        User user = userService.getCurrentLoginUser();

        CommunityResponseDTO result = communityRepository.findDTOByIdForUser(communityId, user.getId())
                .orElseThrow(ErrorCode.NOT_FOUND);

        eventPublisher.publishEvent(new PostViewedEvent(result.postId(), ipAddress));

        return result.applyBlindFilter();
    }

    @Transactional
    public CommunityResponseDTO createPost(CommunityRequestDTO requestDTO, BoardType boardType) {
        User user = userService.getCurrentLoginUser();

        Post post = Post.from(requestDTO, user, boardType);

        Post savedPost = postRepository.save(post);

        CommunityPost communityPost = CommunityPost.from(savedPost);
        CommunityPost savedCommunityPost = communityRepository.save(communityPost);

        PostStatistics postStatistics = PostStatistics.from(savedPost);
        postStatisticsRepository.save(postStatistics);

        CompletableFuture.runAsync(() -> {
            try {
                postEmbeddingService.createPostEmbeddings(savedPost);
            } catch (Exception e) {
                log.warn("커뮤니티 게시글 임베딩 생성 실패: postId={}", savedPost.getId(), e);
            }
        });

        List<String> imgUrls = fileService.extractImageUrls(requestDTO.content());
        fileService.savePostImage(savedPost, imgUrls);

        return CommunityResponseDTO.from(savedCommunityPost, false, false);
    }

    @Transactional
    public void updatePost(CommunityRequestDTO requestDTO, Long communityId) {
        CommunityPost communityPost = findById(communityId);

        Post post = communityPost.getPost();
        validatePost(post);
        if(post.getIsBlinded()){
            throw ErrorCode.IS_BLINDED_POST.get();
        }

        post.update(requestDTO);
    }

    @Transactional
    public void deletePost(Long communityId) {
        CommunityPost communityPost = findById(communityId);

        Post post = communityPost.getPost();
        validatePost(post);

        postStatisticsRepository.deleteById(post.getId());
        bookmarkRepository.deleteByPost_Id(post.getId());
        likeRepository.deleteByPost_Id(post.getId());
        reportRepository.deleteByPost_Id(post.getId());
        commentRepository.deleteByPostId(post.getId());
        fileService.deletePostImage(post);
        postRepository.delete(post);
    }

    private void validatePost(Post post) {
        User user = userService.getCurrentLoginUser();

        if (!canEditOrDelete(post, user)) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    private boolean canEditOrDelete(Post post, User user) {
        return Objects.equals(post.getUser().getId(), user.getId()) || user.getRole() == Role.ADMIN;
    }

    public CommunityPost findById(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(ErrorCode.NOT_FOUND);
    }
}

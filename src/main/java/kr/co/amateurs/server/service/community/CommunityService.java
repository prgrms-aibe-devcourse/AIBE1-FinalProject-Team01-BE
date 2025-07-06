package kr.co.amateurs.server.service.community;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.CommunityPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.community.CommunityRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.ai.PostEmbeddingService;
import kr.co.amateurs.server.service.file.FileService;
import kr.co.amateurs.server.service.post.PostService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final BookmarkService bookmarkService;
    private final LikeService likeService;

    private final UserService userService;
    private final PostEmbeddingService postEmbeddingService;
    private final FileService fileService;

    public PageResponseDTO<CommunityResponseDTO> searchPosts(BoardType boardType, PostPaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        String keyword = paginationParam.getKeyword();
        Page<CommunityPost> communityPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            communityPage = communityRepository.findByContentAndBoardType(keyword.trim(), boardType, pageable);
        } else {
            communityPage = communityRepository.findByBoardType(boardType, pageable);
        }

        return convertPageToDTO(communityPage.map(communityPost -> CommunityResponseDTO.from(communityPost, false, false)));
    }

    public CommunityResponseDTO getPost(Long communityId) {
        Optional<User> user = userService.getCurrentUser();

        CommunityPost communityPost = findById(communityId);

        boolean hasBookmarked = false;
        boolean hasLiked = false;
        if (user.isPresent()) {
            hasBookmarked = bookmarkService.checkHasBookmarked(communityPost.getPost().getId(), user.get().getId());
            hasLiked = likeService.checkHasLiked(communityPost.getPost().getId(), user.get().getId());
        }

        return CommunityResponseDTO.from(communityPost, hasLiked, hasBookmarked);
    }

    @Transactional
    public CommunityResponseDTO createPost(CommunityRequestDTO requestDTO, BoardType boardType) {
        User user = userService.getCurrentLoginUser();

        Post post = Post.from(requestDTO, user, boardType);

        Post savedPost = postRepository.save(post);

        CommunityPost communityPost = CommunityPost.from(savedPost);
        CommunityPost savedCommunityPost = communityRepository.save(communityPost);

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

        post.update(requestDTO);
    }

    @Transactional
    public void deletePost(Long communityId) {
        CommunityPost communityPost = findById(communityId);

        Post post = communityPost.getPost();
        validatePost(post);

        communityRepository.delete(communityPost);
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

    // TODO - 사용자가 한 게시글을 여러번 조회할 경우 viewCount를 중복으로 올라가도록 할 지 한 명당 1회만 올라가게 할 지 정해야 함
    //         + 조회수 증가를 별도의 API로 할 지 다른 곳에다 붙일 지 정해야 함
    @Transactional
    public void increaseViewCount(Long postId) {
        postRepository.increaseViewCount(postId);
    }
}

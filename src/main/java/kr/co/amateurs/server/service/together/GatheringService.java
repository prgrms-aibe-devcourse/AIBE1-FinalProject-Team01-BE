package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    private final UserService userService;

    public PageResponseDTO<GatheringPostResponseDTO> getGatheringPostList(PostPaginationParam paginationParam) {
        Page<GatheringPost> gpPage = gatheringRepository.findAllByKeyword(paginationParam.getKeyword(), paginationParam.toPageable());
        Page<GatheringPostResponseDTO> response = gpPage.map(gp->convertToDTO(gp, gp.getPost(), false, false));
        return convertPageToDTO(response);
    }


    public GatheringPostResponseDTO getGatheringPost(Long id) {
        User user = userService.getCurrentLoginUser();

        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();
        return convertToDTO(gp, post, likeService.checkHasLiked(post.getId(), user.getId()), bookmarkService.checkHasBookmarked(post.getId(), user.getId()));
    }


    @Transactional
    public GatheringPostResponseDTO createGatheringPost(GatheringPostRequestDTO dto) {
        User currentUser = userService.getCurrentLoginUser();
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

        return convertToDTO(savedGp, savedPost, false, false);
    }


    @Transactional
    public void updateGatheringPost(Long id, GatheringPostRequestDTO dto) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();
        validateUser(post.getUser().getId());
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
        post.update(updatePostDTO);
        gp.update(dto);
    }

    @Transactional
    public void deleteGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND);
        Post post = gp.getPost();
        validateUser(post.getUser().getId());
        gatheringRepository.deleteById(gp.getId());
        postRepository.deleteById(post.getId());
    }

    private void validateUser(Long userId) {
        User currentUser = userService.getCurrentLoginUser();
        Long currentId = currentUser.getId();
        Role currentRole = currentUser.getRole();
        if (!currentId.equals(userId) && currentRole != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "해당 게시글에 접근할 수 없습니다.");
        }
    }
}


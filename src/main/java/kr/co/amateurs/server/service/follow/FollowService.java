package kr.co.amateurs.server.service.follow;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.domain.dto.post.PostResponseDTO;
import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.follow.FollowRepository;
import kr.co.amateurs.server.repository.post.PostJooqRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO.convertToFollowingDTO;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostStatisticsRepository postStatisticsRepository;
    private final PostJooqRepository postJooqRepository;

    private final UserService userService;

    public List<FollowResponseDTO> getFollowerList() {
        User user = userService.getCurrentLoginUser();
        List<Follow> followList = followRepository.findByToUser(user);
        return followList.stream().map(FollowResponseDTO::convertToFollowerDTO).toList();
    }

    public List<FollowResponseDTO> getFollowingList() {
        User user = userService.getCurrentLoginUser();
        List<Follow> followList = followRepository.findByFromUser(user);
        return followList.stream().map(FollowResponseDTO::convertToFollowingDTO).toList();
    }

    public PageResponseDTO<PostResponseDTO> getFollowPostList(PaginationParam paginationParam) {
        User user = userService.getCurrentLoginUser();
        Pageable pageable = paginationParam.toPageable();

        Page<PostResponseDTO> postResponseDTO = postJooqRepository.findPostsByType(user.getId(), pageable, "follow", user.getRole())
                .map(PostResponseDTO::applyBlindFilter);

        return convertPageToDTO(postResponseDTO);
    }

    @Transactional
    public FollowResponseDTO followUser(Long targetUserId){
        User currentUser = userService.getCurrentLoginUser();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(ErrorCode.USER_NOT_FOUND);
        if(currentUser.getId().equals(targetUser.getId())){
            throw new CustomException(ErrorCode.SELF_FOLLOW);
        }
        Follow follow = Follow.builder().fromUser(currentUser).toUser(targetUser).build();
        followRepository.save(follow);
        return convertToFollowingDTO(follow);
    }

    @Transactional
    public void unfollowUser(Long targetUserId){
        User currentUser = userService.getCurrentLoginUser();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(ErrorCode.USER_NOT_FOUND);
        followRepository.deleteByToUserAndFromUser(targetUser, currentUser);
    }

    private PostResponseDTO filterByUserRole(PostResponseDTO  post, Role role) {
        Set<BoardType> boardTypes = accessibleBoardType(role);

        if (!boardTypes.contains(post.boardType())) {
            return null;
        }
        return post;
    }

    private Set<BoardType> accessibleBoardType(Role role) {
        return switch (role) {
            case ADMIN, STUDENT -> EnumSet.allOf(BoardType.class);
            case GUEST -> EnumSet.of(
                    BoardType.REVIEW,
                    BoardType.PROJECT_HUB,
                    BoardType.NEWS,
                    BoardType.FREE,
                    BoardType.QNA,
                    BoardType.RETROSPECT
            );
            case ANONYMOUS -> EnumSet.of(
                    BoardType.REVIEW,
                    BoardType.PROJECT_HUB,
                    BoardType.NEWS
            );
        };
    }

}

package kr.co.amateurs.server.service.follow;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.PostStatistics;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.follow.FollowRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO.convertToFollowingDTO;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostStatisticsRepository postStatisticsRepository;

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

    public List<FollowPostResponseDTO> getFollowPostList() {
        User user = userService.getCurrentLoginUser();
        List<Long> followingUserId = followRepository.findByFromUser(user)
                .stream()
                .map(follow -> follow.getToUser().getId())
                .toList();
        if (followingUserId.isEmpty()) {
            return Collections.emptyList();
        }
        List<Post> allPosts = postRepository.findByUserIdIn(followingUserId);
        List<Post> filteredPosts = filterByUserRole(allPosts, user.getRole());

        List<Long> postIds = filteredPosts.stream()
                .map(Post::getId)
                .toList();

        List<PostStatistics> statisticsList = postStatisticsRepository.findByPostIdIn(postIds);
        Map<Long, PostStatistics> statisticsMap = statisticsList.stream()
                .collect(Collectors.toMap(PostStatistics::getPostId, Function.identity()));

        return filteredPosts.stream()
                .map(post -> {
                    PostStatistics postStatistics = statisticsMap.get(post.getId());
                    return FollowPostResponseDTO.convertToDTO(post, postStatistics);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FollowResponseDTO followUser(Long targetUserId){
        User currentUser = userService.getCurrentLoginUser();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(ErrorCode.USER_NOT_FOUND);
        if(currentUser.equals(targetUser)){
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

    private List<Post> filterByUserRole(List<Post> posts, Role role) {
        Set<BoardType> boardTypes = accessibleBoardType(role);

        return posts.stream()
                .filter(post -> boardTypes.contains(post.getBoardType()))
                .collect(Collectors.toList());
    }

    private Set<BoardType> accessibleBoardType(Role role) {
        return switch (role) {
            case ADMIN, STUDENT -> EnumSet.allOf(BoardType.class);
            case GUEST -> EnumSet.of(
                    BoardType.REVIEW,
                    BoardType.PROJECT_HUB,
                    BoardType.INFO,
                    BoardType.FREE,
                    BoardType.QNA,
                    BoardType.RETROSPECT
            );
            case ANONYMOUS -> EnumSet.of(
                    BoardType.REVIEW,
                    BoardType.PROJECT_HUB,
                    BoardType.INFO
            );
        };
    }

}

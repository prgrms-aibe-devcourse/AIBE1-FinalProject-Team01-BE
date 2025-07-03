package kr.co.amateurs.server.service.follow;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO;
import kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO;
import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.follow.FollowRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static kr.co.amateurs.server.domain.dto.follow.FollowPostResponseDTO.convertToDTO;
import static kr.co.amateurs.server.domain.dto.follow.FollowResponseDTO.convertToFollowingDTO;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final UserService userService;

    public List<FollowResponseDTO> getFollowerList(Long userId) {
        User user = userService.findById(userId);
        List<Follow> followList = followRepository.findByToUser(user);
        return followList.stream().map(FollowResponseDTO::convertToFollowerDTO).toList();
    }

    public List<FollowResponseDTO> getFollowingList(Long userId) {
        User user = userService.findById(userId);
        List<Follow> followList = followRepository.findByFromUser(user);
        return followList.stream().map(FollowResponseDTO::convertToFollowingDTO).toList();
    }

    public List<FollowPostResponseDTO> getFollowPostList(Long userId) {
        User user = userService.findById(userId);
        return followRepository.findByFromUser(user).stream()
                .map(Follow::getToUser)
                .flatMap(followingUser -> postRepository.findByUser(followingUser).stream())
                .map(FollowPostResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FollowResponseDTO followUser(Long targetUserId){
        User currentUser = userService.getCurrentLoginUser();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(ErrorCode.USER_NOT_FOUND);
        Follow follow = Follow.builder().fromUser(currentUser).toUser(targetUser).build();
        followRepository.save(follow);
        return convertToFollowingDTO(follow);
    }

    @Transactional
    public void unfollowUser(Long targetUserId){
        User currentUser = userService.getCurrentLoginUser();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(ErrorCode.USER_NOT_FOUND);
        Follow follow = followRepository.findByToUserAndFromUser(targetUser, currentUser);
        followRepository.delete(follow);
    }

}

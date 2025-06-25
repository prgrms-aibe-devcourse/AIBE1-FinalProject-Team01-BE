package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataCollectService {

    private final BookmarkService bookmarkService;
    private final PostService postService;
    private final LikeService likeService;
    private final UserService userService;

    public List<PostContentData> collectUserActivities(Long userId) {
        List<PostContentData> allActivities = new ArrayList<>();

        allActivities.addAll(bookmarkService.getBookmarkedPosts(userId));
        allActivities.addAll(likeService.getLikedPosts(userId));
        allActivities.addAll(postService.getWritePosts(userId));

        log.info("총 데이터 개수 : userId={}, {}개", userId, allActivities.size());
        return allActivities;
    }

    public String collectUserTopics(Long userId) {
        return userService.getUserTopics(userId);
    }

    public String collectDevcourseName(Long userId) {
        return userService.getDevcourseName(userId);
    }
}

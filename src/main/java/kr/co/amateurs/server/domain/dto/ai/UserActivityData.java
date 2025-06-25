package kr.co.amateurs.server.domain.dto.ai;

import java.util.List;

public record UserActivityData(
        String userTopics,
        List<String> recentPosts,
        List<String> likedPosts,
        List<String> bookmarkedPosts
) {
}

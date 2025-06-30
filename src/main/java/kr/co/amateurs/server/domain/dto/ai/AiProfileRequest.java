package kr.co.amateurs.server.domain.dto.ai;

import java.util.List;

public record AiProfileRequest(
        String userTopics,
        String devcourseName,
        List<PostSummaryData> activitySummaries
) {
}

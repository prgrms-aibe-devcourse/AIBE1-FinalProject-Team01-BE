package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.AiProfileRequest;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.ai.PostSummaryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiLlmService {

    private final AiPostAnalysis aiPostAnalysis;

    public PostSummaryData summarizeBookmarkedPosts(List<PostContentData> bookmarkedPosts) {
        if (bookmarkedPosts.isEmpty()) {
            return new PostSummaryData("북마크", "북마크한 게시물이 없습니다.");
        }

        try {
            String postsContent = formatPostData(bookmarkedPosts);
            String summary = aiPostAnalysis.analyzeBookmarks(postsContent);

            log.info("북마크 게시물 요약 완료: {} 개 게시물", bookmarkedPosts.size());
            return new PostSummaryData("북마크", summary);

        } catch (Exception e) {
            log.error("북마크 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_SUMMARIZE.get();
        }
    }

    public PostSummaryData summarizeLikedPosts(List<PostContentData> likedPosts) {
        if (likedPosts.isEmpty()) {
            return new PostSummaryData("좋아요", "좋아요한 게시물이 없습니다.");
        }

        try {
            String postsContent = formatPostData(likedPosts);
            String summary = aiPostAnalysis.analyzeLikes(postsContent);

            log.info("좋아요 게시물 요약 완료: {} 개 게시물", likedPosts.size());
            return new PostSummaryData("좋아요", summary);

        } catch (Exception e) {
            log.error("좋아요 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_SUMMARIZE.get();
        }
    }

    public PostSummaryData summarizeWrittenPosts(List<PostContentData> writtenPosts) {
        if (writtenPosts.isEmpty()) {
            return new PostSummaryData("작성글", "작성한 게시물이 없습니다.");
        }

        try {
            String postsContent = formatPostData(writtenPosts);
            String summary = aiPostAnalysis.analyzeWritten(postsContent);

            log.info("작성 게시물 요약 완료: {} 개 게시물", writtenPosts.size());
            return new PostSummaryData("작성글", summary);

        } catch (Exception e) {
            log.error("작성 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_SUMMARIZE.get();
        }
    }

    public AiProfileResponse generateFinalProfile(AiProfileRequest request) {
        try {
            String summariesText = request.activitySummaries().stream()
                    .map(summary -> String.format("- %s: %s", summary.activityType(), summary.summary()))
                    .collect(Collectors.joining("\n"));

            AiProfileResponse profile = aiPostAnalysis.generateFinalProfile(
                    request.userTopics(),
                    request.devcourseName(),
                    summariesText
            );

            log.info("최종 AI 프로필 생성 완료");
            return profile;

        } catch (Exception e) {
            log.error("최종 AI 프로필 생성 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE_GENERATION.get();
        }
    }

    private String formatPostData(List<PostContentData> posts) {
        return posts.stream()
                .map(post -> String.format("제목: %s\n내용: %s", post.title(), post.content()))
                .collect(Collectors.joining("\n\n"));
    }
}
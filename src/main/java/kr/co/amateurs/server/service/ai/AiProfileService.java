package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.AiProfileRequest;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.ai.PostSummaryData;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiProfileService {

    private final BookmarkService bookmarkService;
    private final PostService postService;
    private final LikeService likeService;
    private final UserService userService;
    private final AiLlmService aiLlmService;
    private final AiProfileRepository aiProfileRepository;

    private String collectUserTopics(Long userId) {
        return userService.getUserTopics(userId);
    }

    private String collectDevcourseName(Long userId) {
        return userService.getDevcourseName(userId);
    }

    @Transactional
    public AiProfile generateInitialProfile(Long userId) {
        try {
            log.info("초기 AI 프로필 생성 시작: userId={}", userId);

            String userTopics = collectUserTopics(userId);
            AiProfileResponse profile = aiLlmService.generateInitialProfile(userTopics);
            log.info("초기 AI 프로필 생성 완료: userId={}, profile={}", userId, profile);

            return saveOrUpdateProfile(userId, profile);

        } catch (Exception e) {
            log.error("초기 AI 프로필 생성 실패: userId={}", userId, e);
            throw ErrorCode.ERROR_AI_PROFILE_GENERATION.get();
        }
    }


    @Transactional
    public AiProfile generateCompleteUserProfile(Long userId) {
        try {
            log.info("사용자 AI 프로필 생성 시작: userId={}", userId);

            PostSummaryData bookmarkSummary = collectAndAnalyzeBookmarks(userId);
            PostSummaryData likeSummary = collectAndAnalyzeLikes(userId);
            PostSummaryData writtenSummary = collectAndAnalyzeWritten(userId);
            log.info("사용자 AI 프로필 데이터 수집 완료: userId={}", userId);

            String userTopics = collectUserTopics(userId);;
            String devcourseName = collectDevcourseName(userId);
            log.info("사용자 정보 확인: userId={}, devcourseName={}", userId, devcourseName);


            List<PostSummaryData> summaries = List.of(bookmarkSummary, likeSummary, writtenSummary);
            AiProfileRequest request = new AiProfileRequest(userTopics, devcourseName, summaries);
            AiProfileResponse profile = aiLlmService.generateFinalProfile(request);
            log.info("AI 프로필 생성 1단계 완료: userId={}, profile={}", userId, profile);

            return saveOrUpdateProfile(userId, profile);
        } catch (Exception e) {
            log.error("사용자 AI 프로필 생성 실패: userId={}", userId, e);
            throw ErrorCode.ERROR_AI_PROFILE_GENERATION.get();
        }
    }

    private PostSummaryData collectAndAnalyzeBookmarks(Long userId) {
        try {
            List<PostContentData> bookmarks = bookmarkService.getBookmarkedPosts(userId);
            log.info("북마크 데이터 수집 완료: {} 개", bookmarks.size());
            return aiLlmService.summarizeBookmarkedPosts(bookmarks);

        } catch (Exception e) {
            log.warn("북마크 수집 실패, 기본값 사용: userId={}", userId, e);
            return new PostSummaryData("북마크", "북마크 활동이 없습니다.");
        }
    }

    private PostSummaryData collectAndAnalyzeLikes(Long userId) {
        try {
            List<PostContentData> likes = likeService.getLikedPosts(userId);
            log.info("좋아요 데이터 수집 완료: {} 개", likes.size());
            return aiLlmService.summarizeLikedPosts(likes);

        } catch (Exception e) {
            log.warn("좋아요 수집 실패, 기본값 사용: userId={}", userId, e);
            return new PostSummaryData("좋아요", "좋아요 활동이 없습니다.");
        }
    }

    private PostSummaryData collectAndAnalyzeWritten(Long userId) {
        try {
            List<PostContentData> written = postService.getWritePosts(userId);
            log.info("작성글 데이터 수집 완료: {} 개", written.size());
            return aiLlmService.summarizeWrittenPosts(written);

        } catch (Exception e) {
            log.warn("작성글 수집 실패, 기본값 사용: userId={}", userId, e);
            return new PostSummaryData("작성글", "작성 활동이 없습니다.");
        }
    }

    /**
     * AI 프로필을 생성하고 Response DTO를 반환합니다.
     */
    @Transactional
    public AiProfileResponse generateUserProfileResponse(Long userId) {
        AiProfile savedProfile = generateCompleteUserProfile(userId);
        return new AiProfileResponse(
                savedProfile.getPersonaDescription(),
                savedProfile.getInterestKeywords()
        );
    }

    private AiProfile saveOrUpdateProfile(Long userId, AiProfileResponse profile) {
        User user = userService.findById(userId);
        AiProfile aiProfile = aiProfileRepository.findByUserId(userId)
                .map(exist -> {
                    exist.updateProfile(profile);
                    return exist;
                })
                .orElseGet(() -> AiProfile.builder()
                        .user(user)
                        .personaDescription(profile.personaDescription())
                        .interestKeywords(profile.interestKeywords())
                        .build());

        return aiProfileRepository.save(aiProfile);
    }

    /**
     * 최근 활동 여부 체크 (북마크 OR 좋아요 OR 작성글)
     */
    public boolean hasRecentActivity(Long userId, int days) {
        try {
            return bookmarkService.hasRecentBookmarkActivity(userId, days) ||
                    likeService.hasRecentLikeActivity(userId, days) ||
                    postService.hasRecentPostActivity(userId, days);
        } catch (Exception e) {
            log.warn("활동 체크 실패: userId={}", userId, e);
            return false;
        }
    }

}

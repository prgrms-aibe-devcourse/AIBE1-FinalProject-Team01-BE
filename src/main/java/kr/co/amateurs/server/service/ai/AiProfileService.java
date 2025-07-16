package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.ai.AiProfileRequest;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.ai.PostSummaryData;
import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.event.AiProfileEvent;
import kr.co.amateurs.server.repository.ai.AiProfileRepository;
import kr.co.amateurs.server.service.UserService;
import kr.co.amateurs.server.service.bookmark.BookmarkService;
import kr.co.amateurs.server.service.like.LikeService;
import kr.co.amateurs.server.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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


    @Transactional
    public AiProfileResponse generateInitialProfile(Long userId) {
        log.info("초기 AI 프로필 직접 생성: userId={}", userId);
        return executeInitialProfileGeneration(userId);
    }

    /**
     * 초기 프로필 생성 공통 로직
     */
    private AiProfileResponse executeInitialProfileGeneration(Long userId) {
        try {
            String userTopics = collectUserTopics(userId);
            AiProfileResponse profile = aiLlmService.generateInitialProfile(userTopics);
            saveOrUpdateProfile(userId, profile);
            return profile;
        } catch (Exception e) {
            log.error("초기 프로필 생성 실행 실패: userId={}", userId, e);
            throw ErrorCode.ERROR_AI_PROFILE_GENERATION.get();
        }
    }

    /**ㅊㅌㅊㅌㅌㅌ
     * AI 완성 프로필 생성 이벤트 발행
     */
    @Transactional
    public AiProfileResponse generateCompleteUserProfile(Long userId) {
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

            saveOrUpdateProfile(userId, profile);
            return profile;
        } catch (Exception e) {
            log.error("사용자 AI 프로필 생성 실패: userId={}", userId, e);
            throw ErrorCode.ERROR_AI_PROFILE_GENERATION.get();
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiProfileGenerationEvent(AiProfileEvent event) {
        log.info("이벤트 수신 - {} 시 AI 프로필 생성 예약: userId={}",
                event.context(), event.userId());

        CompletableFuture.runAsync(() -> {
            processInitialProfileAsync(event.userId(), event.context());
        });
    }

    @Transactional
    public void processInitialProfileAsync(Long userId, String context) {
        try {
            log.info("비동기 초기 프로필 생성 시작: userId={}, context={}", userId, context);
            executeInitialProfileGeneration(userId);
            log.info("비동기 초기 프로필 생성 완료: userId={}, context={}", userId, context);
        } catch (Exception e) {
            log.error("비동기 초기 프로필 생성 실패: userId={}, context={}", userId, context, e);
        }
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

    private String collectUserTopics(Long userId) {
        return userService.getUserTopics(userId);
    }

    private String collectDevcourseName(Long userId) {
        return userService.getDevcourseName(userId);
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

    private void saveOrUpdateProfile(Long userId, AiProfileResponse profile) {
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

        aiProfileRepository.save(aiProfile);
    }


}

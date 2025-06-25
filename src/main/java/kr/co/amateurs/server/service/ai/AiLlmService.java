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

    private final GeminiClientService geminiClientService;

    public PostSummaryData summarizeBookmarkedPosts(List<PostContentData> bookmarkedPosts) {
        if (bookmarkedPosts.isEmpty()) {
            return new PostSummaryData("북마크", "북마크한 게시물이 없습니다.");
        }

        try {
            String prompt = buildBookmarkSummaryPrompt(bookmarkedPosts);
            String summary = geminiClientService.generateContent(prompt);

            log.info("북마크 게시물 요약 완료: {} 개 게시물", bookmarkedPosts.size());
            return new PostSummaryData("북마크", summary);

        } catch (Exception e) {
            log.error("북마크 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    public PostSummaryData summarizeLikedPosts(List<PostContentData> likedPosts) {
        if (likedPosts.isEmpty()) {
            return new PostSummaryData("좋아요", "좋아요한 게시물이 없습니다.");
        }

        try {
            String prompt = buildLikeSummaryPrompt(likedPosts);
            String summary = geminiClientService.generateContent(prompt);

            log.info("좋아요 게시물 요약 완료: {} 개 게시물", likedPosts.size());
            return new PostSummaryData("좋아요", summary);

        } catch (Exception e) {
            log.error("좋아요 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    public PostSummaryData summarizeWrittenPosts(List<PostContentData> writtenPosts) {
        if (writtenPosts.isEmpty()) {
            return new PostSummaryData("작성글", "작성한 게시물이 없습니다.");
        }

        try {
            String prompt = buildWrittenSummaryPrompt(writtenPosts);
            String summary = geminiClientService.generateContent(prompt);

            log.info("작성 게시물 요약 완료: {} 개 게시물", writtenPosts.size());
            return new PostSummaryData("작성글", summary);

        } catch (Exception e) {
            log.error("작성 게시물 요약 생성 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    public AiProfileResponse generateFinalProfile(AiProfileRequest request) {
        try {
            String prompt = buildProfileGenerationPrompt(request);
            String fullProfile = geminiClientService.generateContent(prompt);

            String[] profileParts = parseProfileResponse(fullProfile);

            log.info("최종 AI 프로필 생성 완료");
            return new AiProfileResponse(profileParts[0], profileParts[1]);

        } catch (Exception e) {
            log.error("최종 AI 프로필 생성 실패", e);
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    // 파싱
    private String[] parseProfileResponse(String fullProfile) {
        try {
            String personaDescription = findJsonValue(fullProfile, "persona_description");
            String interestKeywords = findJsonValue(fullProfile, "interest_keywords");

            return new String[]{personaDescription, interestKeywords};

        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 기본값 사용: {}", e.getMessage());
            return new String[]{fullProfile, ""};
        }
    }

    private String findJsonValue(String text, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException(key + " 값을 찾을 수 없습니다");
    }


    private String buildBookmarkSummaryPrompt(List<PostContentData> posts) {
        String postsContent = posts.stream()
                .map(post -> String.format("제목: %s\n내용: %s", post.title(), post.content()))
                .collect(Collectors.joining("\n\n"));

        return String.format("""
            다음은 사용자가 북마크한 게시물들입니다.
            사용자가 북마크하는 게시물의 특징과 관심사를 분석해서 간단히 요약해주세요.
            
            북마크한 게시물들:
            %s
            
            요구사항:
            - 사용자가 어떤 주제에 관심이 있는지 파악
            - 북마크하는 게시물의 패턴이나 특징 분석
            - 3-4문장으로 간결하게 요약
            - 구체적인 키워드나 기술 스택 언급
            - 마크다운 형식 사용 금지, 일반 텍스트로만 응답
            """, postsContent);
    }

    private String buildLikeSummaryPrompt(List<PostContentData> posts) {
        String postsContent = posts.stream()
                .map(post -> String.format("제목: %s\n내용: %s", post.title(), post.content()))
                .collect(Collectors.joining("\n\n"));

        return String.format("""
            다음은 사용자가 좋아요를 누른 게시물들입니다.
            사용자가 어떤 내용에 흥미를 느끼는지 분석해서 간단히 요약해주세요.
            
            좋아요한 게시물들:
            %s
            
            요구사항:
            - 사용자가 공감하거나 흥미로워하는 내용의 패턴 분석
            - 좋아요를 누르는 게시물의 특성 파악
            - 3-4문장으로 간결하게 요약
            - 구체적인 관심 분야나 기술 언급
            - 마크다운 형식 사용 금지, 일반 텍스트로만 응답
            """, postsContent);
    }

    private String buildWrittenSummaryPrompt(List<PostContentData> posts) {
        String postsContent = posts.stream()
                .map(post -> String.format("제목: %s\n내용: %s", post.title(), post.content()))
                .collect(Collectors.joining("\n\n"));

        return String.format("""
            다음은 사용자가 직접 작성한 게시물들입니다.
            사용자의 전문성, 관심사, 작성 스타일을 분석해서 간단히 요약해주세요.
            
            작성한 게시물들:
            %s
            
            요구사항:
            - 사용자의 전문 분야나 강점 파악
            - 주로 다루는 주제나 기술 스택 분석
            - 질문하는 분야 및 작성자의 수준 파악
            - 3-4문장으로 간결하게 요약
            - 마크다운 형식 사용 금지, 일반 텍스트로만 응답
            """, postsContent);
    }

    private String buildProfileGenerationPrompt(AiProfileRequest request) {
        String summariesText = request.activitySummaries().stream()
                .map(summary -> String.format("- %s: %s", summary.activityType(), summary.summary()))
                .collect(Collectors.joining("\n"));

        return String.format("""
                **역할**: 당신은 IT 커뮤니티 사용자의 활동 데이터를 분석하여 해당 사용자의 관심사와 특성을 심층적으로 파악하는 전문 분석가입니다.
                
                **입력 데이터**:
                ---
                **사용자 가입 토픽**: %s
                **데브코스 정보**: %s
                
                **사용자 활동 요약**:
                %s
                ---
                
                **지시사항**:
                1. **사용자 특성 분석**: 위 데이터를 종합하여 이 사용자가 어떤 유형의 개발자/기술자이며, 어떤 특성을 가지고 있는지 1~2문장으로 간결하게 설명해주세요.
                
                2. **관심 기술 키워드 추출**: 사용자의 활동 내역과 가입 토픽을 바탕으로, 이 사용자가 관심을 가질 만한 구체적인 IT 기술 키워드들을 콤마(,)로 구분하여 5~10개 정도 추출해주세요.
                
                **출력 형식**:
                반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트는 포함하지 마세요.
                
                {
                  "persona_description": "[분석된 사용자 특성]",
                  "interest_keywords": "[추출된 관심 기술 키워드 리스트, 콤마로 구분]"
                }
                """, request.userTopics(), request.devcourseName(), summariesText);
    }
}
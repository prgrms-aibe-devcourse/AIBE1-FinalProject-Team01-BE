package kr.co.amateurs.server.service.ai;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;

public interface AiPostAnalysis {

    @UserMessage("""
        다음은 사용자가 북마크한 게시물들입니다.
        사용자가 북마크하는 게시물의 특징과 관심사를 분석해서 간단히 요약해주세요.
        
        북마크한 게시물들:
        {{posts}}
        
        요구사항:
        - 사용자가 어떤 주제에 관심이 있는지 파악
        - 북마크하는 게시물의 패턴이나 특징 분석
        - 3-4문장으로 간결하게 요약
        - 구체적인 키워드나 기술 스택 언급
        """)
    String analyzeBookmarks(@V("posts") String posts);

    @UserMessage("""
        다음은 사용자가 좋아요를 누른 게시물들입니다.
        사용자가 어떤 내용에 흥미를 느끼는지 분석해서 간단히 요약해주세요.
        
        좋아요한 게시물들:
        {{posts}}
        
        요구사항:
        - 사용자가 공감하거나 흥미로워하는 내용의 패턴 분석
        - 좋아요를 누르는 게시물의 특성 파악
        - 3-4문장으로 간결하게 요약
        - 구체적인 관심 분야나 기술 언급
        """)
    String analyzeLikes(@V("posts") String posts);

    @UserMessage("""
        다음은 사용자가 직접 작성한 게시물들입니다.
        사용자의 전문성, 관심사, 작성 스타일을 분석해서 간단히 요약해주세요.
        
        작성한 게시물들:
        {{posts}}
        
        요구사항:
        - 사용자의 전문 분야나 강점 파악
        - 주로 다루는 주제나 기술 스택 분석
        - 질문하는 분야 및 작성자의 수준 파악
        - 3-4문장으로 간결하게 요약
        """)
    String analyzeWritten(@V("posts") String posts);

    @UserMessage("""
        사용자가 가입 시 선택한 토픽을 기반으로 기본 AI 프로필을 생성해주세요.
        
        선택한 토픽: {{userTopics}}
        
        요구사항:
        - 선택한 토픽을 기반으로 기본적인 관심사 추론
        - 해당 분야의 일반적인 학습 방향 제시
        - 관심 기술 키워드는 선택한 토픽 관련 3-5개로 구성
        - 아직 활동 데이터가 없으므로 일반적인 초보자 관점에서 작성
        """)
    AiProfileResponse generateInitialProfile(@V("userTopics") String userTopics);

    @UserMessage("""
        사용자의 활동 데이터를 분석하여 개인화된 프로필을 생성해주세요.
        
        사용자 가입 토픽: {{userTopics}}
        데브코스 정보: {{devcourseName}}
        
        사용자 활동 요약:
        {{summaries}}
        
        요구사항:
        - 데브코스 정보가 "정보 없음"인 경우 무시하고 다른 정보로 분석
        - 활동 요약이 부족한 경우 토픽 정보를 더 중점적으로 활용
        - 관심 기술 키워드는 5~8개 이내로 작성
        """)
    AiProfileResponse generateFinalProfile(@V("userTopics") String userTopics, @V("devcourseName") String devcourseName, @V("summaries") String summaries);
}
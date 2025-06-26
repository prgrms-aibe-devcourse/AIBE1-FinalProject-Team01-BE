package kr.co.amateurs.server.service.ai;

import dev.langchain4j.service.UserMessage;
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
    String analyzeBookmarks(String posts);

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
    String analyzeLikes(String posts);

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
    String analyzeWritten(String posts);

    @UserMessage("""
        사용자의 활동 데이터를 분석하여 개인화된 프로필을 생성해주세요.
        
        사용자 가입 토픽: {{userTopics}}
        데브코스 정보: {{devcourseName}}
        
        사용자 활동 요약:
        {{summaries}}
        
        사용자의 특성을 분석하고 관심 기술 키워드를 추출해주세요.
        """)
    AiProfileResponse generateFinalProfile(String userTopics, String devcourseName, String summaries);
}
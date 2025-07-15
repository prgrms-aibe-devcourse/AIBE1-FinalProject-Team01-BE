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
        1. interest_keyword: 선택한 토픽과 관련된 핵심 키워드 3-5개를 쉼표로 구분 (예: "백엔드 개발, Spring Boot, API 설계, 데이터베이스")
    
        2. persona_description: 선택한 토픽에 관심이 있다는 것을 한 문장으로 자연스럽게 표현 (60-100자)
            - 예시: "Spring과 자바 개발에 관심이 있고, 백엔드 개발과 서버 아키텍처에 관심이 있는 개발자"
            - 예시: "react를 활용한 프론트엔드 기술과 사용자 인터페이스 개발을 좋아함"
            - 예시: "AI/ML 기술과 데이터 분석 분야에 흥미를 느끼고 있음"
            
        선택한 토픽을 활용하여 토픽의 내용이 잘 드러날 수 있게 작성.
        """)
    AiProfileResponse generateInitialProfile(@V("userTopics") String userTopics);

    @UserMessage("""
            사용자의 활동 데이터를 분석하여 개인화된 프로필을 생성해주세요.
            
            사용자 가입 토픽: {{userTopics}}
            데브코스 정보: {{devcourseName}}
            
            사용자 활동 요약:
            {{summaries}}
            
                요구사항:
                1. interest_keyword: 활동 데이터에서 자주 등장하는 기술/키워드를 우선으로 하되, 가입 토픽과 데브코스 정보도 고려하여 5-8개 추출
                    - 실제 활동에서 나타난 관심사 > 가입 시 선택한 토픽 순으로 우선순위
                    - 예: "React, TypeScript, AWS, Docker"
                
                2. persona_description: 실제 활동 패턴을 반영한 구체적인 설명 (100-150자)
                   - 어떤 기술 스택을 주로 다루는지
                   - 어떤 종류의 프로젝트나 주제에 관심이 많은지
                   - 활동 데이터에서 드러나는 개발 성향이나 특징
                    - 예: "React와 Node.js로 풀스택 개발을 하며, 팀 프로젝트와 코드 리뷰에 적극적으로 참여하는 개발자"
                실제 활동 데이터를 최우선으로 반영하여 더 정확하고 개인화된 프로필을 만들어주세요.
            
            요구사항:
            - 데브코스 정보가 "정보 없음"인 경우 무시하고 다른 정보로 분석
            - 활동 요약이 부족한 경우 토픽 정보를 더 중점적으로 활용
            - 관심 기술 키워드는 5~8개 이내로 작성
            """)
    AiProfileResponse generateFinalProfile(@V("userTopics") String userTopics, @V("devcourseName") String devcourseName, @V("summaries") String summaries);
}
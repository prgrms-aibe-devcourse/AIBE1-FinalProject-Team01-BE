package kr.co.amateurs.server.config.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.fixture.ai.AiTestFixture;
import kr.co.amateurs.server.service.ai.AiPostAnalysis;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@TestConfiguration
public class AiTestConfig {

    @Bean
    @Primary
    public AiPostAnalysis mockAiPostAnalysis() {
        AiPostAnalysis mock = Mockito.mock(AiPostAnalysis.class);
        
        // 기본 분석 결과 설정
        when(mock.analyzeBookmarks(any())).thenReturn("북마크한 게시글들을 분석한 결과, 주로 백엔드 개발과 Spring Framework에 관심이 많습니다.");
        when(mock.analyzeLikes(any())).thenReturn("좋아요를 누른 게시글들을 분석한 결과, AI와 머신러닝 기술에 관심을 보입니다.");
        when(mock.analyzeWritten(any())).thenReturn("작성한 게시글들을 분석한 결과, 실무 경험을 바탕으로 한 기술 공유를 선호합니다.");
        
        // 초기 프로필 생성
        when(mock.generateInitialProfile(anyString())).thenReturn(
            new AiProfileResponse(
                "관심 주제를 바탕으로 한 데브코스 수강생입니다.",
                "개발,학습,성장"
            )
        );
        
        // 최종 프로필 생성
        when(mock.generateFinalProfile(anyString(), anyString(), anyString())).thenReturn(
            AiTestFixture.createAiProfileResponse()
        );
        
        return mock;
    }

    @Bean
    @Primary
    public EmbeddingModel mockEmbeddingModel() {
        EmbeddingModel mock = Mockito.mock(EmbeddingModel.class);
        
        // 임베딩 응답 Mock 설정
        Embedding mockEmbedding = Mockito.mock(Embedding.class);
        when(mockEmbedding.vector()).thenReturn(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f});
        
        Response<Embedding> mockResponse = Mockito.mock(Response.class);
        when(mockResponse.content()).thenReturn(mockEmbedding);
        
        when(mock.embed(any(TextSegment.class))).thenReturn(mockResponse);
        when(mock.embed(anyString())).thenReturn(mockResponse);
        
        return mock;
    }

    @Bean
    @Primary
    public EmbeddingStore<TextSegment> mockEmbeddingStore() {
        EmbeddingStore<TextSegment> mock = Mockito.mock(EmbeddingStore.class);
        
        // 기본적으로 성공적인 저장 결과 반환
        when(mock.add(any(Embedding.class), any(TextSegment.class))).thenReturn("mock-id");
        
        // 검색 결과 Mock 설정
        EmbeddingMatch<TextSegment> mockMatch = createMockEmbeddingMatch(1L, "1", 0.85);
        EmbeddingSearchResult<TextSegment> mockSearchResult = Mockito.mock(EmbeddingSearchResult.class);
        when(mockSearchResult.matches()).thenReturn(List.of(mockMatch));
        
        when(mock.search(any(EmbeddingSearchRequest.class))).thenReturn(mockSearchResult);
        
        // 삭제 동작
        when(mock.removeAll()).thenReturn(null);
        when(mock.removeAll(any())).thenReturn(null);
        
        return mock;
    }

    private EmbeddingMatch<TextSegment> createMockEmbeddingMatch(Long postId, String userId, double score) {
        TextSegment mockSegment = Mockito.mock(TextSegment.class);
        
        // 메타데이터 Mock 설정
        dev.langchain4j.data.document.Metadata mockMetadata = Mockito.mock(dev.langchain4j.data.document.Metadata.class);
        when(mockMetadata.containsKey("postId")).thenReturn(true);
        when(mockMetadata.containsKey("userId")).thenReturn(true);
        when(mockMetadata.getString("postId")).thenReturn(postId.toString());
        when(mockMetadata.getString("userId")).thenReturn(userId);
        
        when(mockSegment.metadata()).thenReturn(mockMetadata);
        
        EmbeddingMatch<TextSegment> mockMatch = Mockito.mock(EmbeddingMatch.class);
        when(mockMatch.embedded()).thenReturn(mockSegment);
        when(mockMatch.score()).thenReturn(score);
        
        return mockMatch;
    }

    /**
     * 특정 테스트에서 커스텀 검색 결과가 필요할 때 사용할 수 있는 헬퍼 메서드
     */
    public static void setupMockSearchResults(EmbeddingStore<TextSegment> mockStore, List<EmbeddingMatch<TextSegment>> matches) {
        EmbeddingSearchResult<TextSegment> mockSearchResult = Mockito.mock(EmbeddingSearchResult.class);
        when(mockSearchResult.matches()).thenReturn(matches);
        when(mockStore.search(any(EmbeddingSearchRequest.class))).thenReturn(mockSearchResult);
    }
}

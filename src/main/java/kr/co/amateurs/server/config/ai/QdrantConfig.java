package kr.co.amateurs.server.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {
    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.embedding-model}")
    private String embeddingModel;

    @Value("${qdrant.host}")
    private String qdrantUrl;

    @Value("${qdrant.port}")
    private String qdrantPort;

    @Value("${qdrant.api-key}")
    private String qdrantApiKey;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Bean
    public EmbeddingModel getEmbedding() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(embeddingModel)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host(qdrantUrl)
                .port(Integer.parseInt(qdrantPort))
                .apiKey(qdrantApiKey)
                .collectionName(collectionName)
                .useTls(true)
                .build();
    }
}
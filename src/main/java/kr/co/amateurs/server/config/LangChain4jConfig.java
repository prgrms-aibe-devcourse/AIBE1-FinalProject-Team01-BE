package kr.co.amateurs.server.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import kr.co.amateurs.server.domain.dto.ai.AiProfileResponse;
import kr.co.amateurs.server.service.ai.AiPostAnalysis;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static dev.langchain4j.service.output.JsonSchemas.jsonSchemaFrom;

@Configuration
public class LangChain4jConfig {

    @Value("${gemini.key}")
    private String geminiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey)
                .modelName(geminiModel)
                .build();
    }

    @Bean
    public AiPostAnalysis aiPostAnalysis(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(AiPostAnalysis.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}
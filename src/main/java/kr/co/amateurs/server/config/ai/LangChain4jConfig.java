package kr.co.amateurs.server.config.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import kr.co.amateurs.server.service.ai.AiPostAnalysis;
import kr.co.amateurs.server.service.report.llm.ReportAnalysis;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.google-ai-gemini.recommend.api-key}")
    private String geminiKey;

    @Value("${langchain4j.google-ai-gemini.recommend.model-name}")
    private String geminiModel;

    @Value("${langchain4j.google-ai-gemini.report.api-key}")
    private String geminiReportKey;

    @Value("${langchain4j.google-ai-gemini.report.model-name}")
    private String geminiReportModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey)
                .modelName(geminiModel)
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageReportModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiReportKey)
                .modelName(geminiReportModel)
                .build();
    }

    @Bean
    public AiPostAnalysis aiPostAnalysis(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(AiPostAnalysis.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public ReportAnalysis reportAnalysis(ChatLanguageModel chatLanguageReportModel) {
        return AiServices.builder(ReportAnalysis.class)
                .chatLanguageModel(chatLanguageReportModel)
                .build();
    }
}
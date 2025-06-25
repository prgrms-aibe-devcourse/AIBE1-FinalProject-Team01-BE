package kr.co.amateurs.server.service.ai;

import kr.co.amateurs.server.domain.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiClientService {

    @Value("${gemini.key}")
    private String geminiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    public String generateContent(String prompt) {
        try(Client client = createClient()) {
            GenerateContentResponse response = client.models.generateContent(geminiModel, prompt, null);
            return response.text();
        } catch (Exception e) {
            throw ErrorCode.ERROR_AI_PROFILE.get();
        }
    }

    private Client createClient() {
        return Client.builder()
                .apiKey(geminiKey)
                .build();
    }
}

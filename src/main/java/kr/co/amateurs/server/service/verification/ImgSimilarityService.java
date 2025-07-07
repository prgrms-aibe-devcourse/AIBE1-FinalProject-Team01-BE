package kr.co.amateurs.server.service.verification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.dto.verfication.SimilarityAnalysisDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImgSimilarityService {
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ReferenceImgService referenceImgService;

    @Value("${huggingface.api.token}")
    private String apiToken;

    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/google/vit-base-patch16-224";

    public SimilarityAnalysisDTO analyzeSimilarity(byte[] imageBytes) {
        File tempFile = null;
        try {
            log.info("사용자 이미지 바이트 배열 받음: {} bytes", imageBytes.length);
            float[] userFeature = extractImageFeature(imageBytes);


            List<Double> similarities = new ArrayList<>();
            List<byte[]> referenceImages = referenceImgService.getReferenceImages();
            log.info("참조 이미지 개수: {}", referenceImages.size());

            for (byte[] referenceImage : referenceImages) {
                float[] refFeature = extractImageFeature(referenceImage);
                double similarity = calculateCosineSimilarity(userFeature, refFeature);

                log.info("유사도 : {} ", similarity);
                similarities.add(similarity);
            }

            double maxSimilarity = similarities.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double avgSimilarity = similarities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double similarityScore = calculateSimilarityScore(maxSimilarity, avgSimilarity);

            return SimilarityAnalysisDTO.builder()
                    .maxSimilarity(maxSimilarity)
                    .avgSimilarity(avgSimilarity)
                    .similarityScore(similarityScore)
                    .similarities(similarities)
                    .isLayoutSimilar(maxSimilarity > 0.7)
                    .build();

        } catch (Exception e) {
            log.error("유사도 분석 실패: ", e);
            return createErrorResult();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private float[] extractImageFeature(byte[] imageBytes) {
        try {
            log.info("HuggingFace API 호출 시작 - 이미지 크기: {} bytes", imageBytes.length);

            WebClient webClient = webClientBuilder
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .build();

            String response = webClient.post()
                    .uri(MODEL_URL)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(BodyInserters.fromValue(imageBytes))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseFeatureVector(response);
        } catch (Exception e) {
            log.error("Feature 추출 실패: ", e);
            return new float[768]; // ViT-base의 feature dimension
        }
    }

    private float[] parseFeatureVector(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            List<Float> features = new ArrayList<>();

            for (JsonNode classification : jsonNode) {
                features.add(classification.get("score").floatValue());
            }

            float[] arr = new float[features.size()];
            for (int i = 0; i < features.size(); i++) {
                arr[i] = features.get(i);
            }
            return arr;

        } catch (Exception e) {
            log.error("Feature vector 파싱 실패: ", e);
            return new float[1000];
        }
    }

    private double calculateCosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double calculateSimilarityScore(double maxSimilarity, double avgSimilarity) {
        return (maxSimilarity * 0.7) + (avgSimilarity * 0.3);
    }

    private SimilarityAnalysisDTO createErrorResult() {
        return SimilarityAnalysisDTO.builder()
                .maxSimilarity(0.0)
                .avgSimilarity(0.0)
                .similarityScore(0.0)
                .similarities(new ArrayList<>())
                .isLayoutSimilar(false)
                .build();
    }
}

package kr.co.amateurs.server.domain.dto.verfication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityAnalysisDTO {
    private double maxSimilarity;
    private double avgSimilarity;
    private double similarityScore;
    private List<Double> similarities;
    private boolean isLayoutSimilar;
}
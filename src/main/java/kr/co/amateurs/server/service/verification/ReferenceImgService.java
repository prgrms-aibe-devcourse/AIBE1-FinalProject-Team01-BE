package kr.co.amateurs.server.service.verification;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReferenceImgService {

    private List<byte[]> referenceImages;

    @PostConstruct
    public void loadReferenceImages() {
        referenceImages = new ArrayList<>();

        String[] referenceFiles = {
                "data1.jpg",
                "data2.jpg",
                "data3.jpg",
                "data5.png",
                "data6.png"
        };

        for (String fileName : referenceFiles) {
            try {
                ClassPathResource resource = new ClassPathResource("reference-images/" + fileName);
                if (resource.exists()) {
                    byte[] imageBytes = resource.getInputStream().readAllBytes();
                    referenceImages.add(imageBytes);
                    log.info("참조 이미지 로드 완료: {}", fileName);
                }
            } catch (IOException e) {
                log.warn("참조 이미지 로드 실패: {}", fileName, e);
            }
        }

        log.info("총 {}개의 참조 이미지 로드 완료", referenceImages.size());
    }

    public List<byte[]> getReferenceImages() {
        return referenceImages;
    }
}
package kr.co.amateurs.server.controller.file;


import kr.co.amateurs.server.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "post-images") String directory
            ) {
        System.out.println("=== 파일 업로드 요청 받음 ===");
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("파일 크기: " + file.getSize());
        System.out.println("Content Type: " + file.getContentType());
        System.out.println("디렉토리: " + directory);
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("파일 업로드 요청 받음: " + file.getOriginalFilename());

            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest()
                        .body(result);
            }
            String fileUrl = fileService.uploadFile(file, directory);

            result.put("success", true);
            result.put("url", fileUrl);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "파일 업로드 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }


    }

}

package kr.co.amateurs.server.controller.file;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.amateurs.server.domain.dto.file.FileResponseDTO;
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
@Tag(name="File Upload", description = "이미지 파일 업로드 API")
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 S3에 업로드합니다.")
    public ResponseEntity<FileResponseDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "post-images") String directory
            ) throws IOException {
        FileResponseDTO dto = fileService.uploadFile(file, directory);
        return ResponseEntity.ok(dto);
    }

}

package kr.co.amateurs.server.service.file;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileService {

    private String publicUrl;

    public String uploadFile(MultipartFile file, String directoryPath) throws IOException {


        return publicUrl + "/" + directoryPath + "/" + file.getOriginalFilename();
    }
}

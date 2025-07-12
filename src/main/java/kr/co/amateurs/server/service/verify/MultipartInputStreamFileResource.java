package kr.co.amateurs.server.service.verify;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class MultipartInputStreamFileResource extends InputStreamResource {
    private final String filename;
    private final long contentLength;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename, long contentLength) {
        super(inputStream);
        this.filename = filename;
        this.contentLength = contentLength;
    }

    public static MultipartInputStreamFileResource from(MultipartFile file) throws IOException {
        return new MultipartInputStreamFileResource(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getSize()
        );
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() {
        return this.contentLength;
    }
}
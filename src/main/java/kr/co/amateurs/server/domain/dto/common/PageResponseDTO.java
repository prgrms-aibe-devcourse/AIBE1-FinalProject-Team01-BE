package kr.co.amateurs.server.domain.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

// PageImpl 직렬화 문제로 JSON 생성이 안되어 PageDTO 새로 생성
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponseDTO<T> convertPageToDTO(Page<T> page){
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}

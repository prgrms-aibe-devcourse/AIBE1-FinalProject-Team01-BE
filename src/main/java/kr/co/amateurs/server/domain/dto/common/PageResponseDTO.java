package kr.co.amateurs.server.domain.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;


public record PageResponseDTO<T>(
        List<T> content,
        PageInfo pageInfo
) {
    public static <T> PageResponseDTO<T> convertPageToDTO(Page<T> page){
        PageInfo pageInfo = new PageInfo(page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements());
        return new PageResponseDTO<>(
                page.getContent(),
                pageInfo
        );
    }

    public static <T> PageResponseDTO<T> convertPageToDTO(Page<T> page, PageInfo pageInfo){
        return new PageResponseDTO<>(
                page.getContent(),
                pageInfo
        );
    }

    public static <T> PageResponseDTO<T> convertPageToDTO(List<T> content, PageInfo pageInfo) {
        return new PageResponseDTO<>(content, pageInfo);
    }
}

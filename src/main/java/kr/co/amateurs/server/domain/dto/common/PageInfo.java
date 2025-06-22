package kr.co.amateurs.server.domain.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
public class PageInfo {
    @Schema(description = "현재 페이지 번호", example = "1")
    private int pageNumber;

    @Schema(description = "페이지 당 아이템 수", example = "1")
    private int pageSize;

    @Schema(description = "전체 페이지 수", example = "1")
    private int totalPages;

    @Schema(description = "전체 아이템 수", example = "1")
    private long totalElements;

    public static PageInfo from(Page<?> page) {
        Pageable pageable = page.getPageable();

        return new PageInfo(
                pageable.getPageNumber(),
                page.getNumberOfElements(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}

package kr.co.amateurs.server.domain.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationParam {

    @Schema(description = "페이지 번호 (기본값 0)", implementation = Integer.class, example = "0")
    protected Integer page = 0;

    @Schema(description = "페이지당 크기 (기본값 = 10)", implementation = Integer.class, example = "10")
    protected Integer size = 10;

    @Schema(description = "정렬 방법", implementation = Sort.Direction.class)
    protected Sort.Direction sortDirection = Sort.Direction.ASC;

    protected PaginationSortType field = PaginationSortType.EMPTY;

    public Pageable toPageable() {
        return PageRequest.of(page, size, sortDirection, field.getField());
    }
}

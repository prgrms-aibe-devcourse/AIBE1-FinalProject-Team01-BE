package kr.co.amateurs.server.domain.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationParam {

    @Schema(description = "페이지 번호 (기본값 0)", implementation = Integer.class, example = "0")
    @Min(0)
    @Builder.Default
    protected Integer page = 0;

    @Schema(description = "페이지당 크기 (기본값 = 10)", implementation = Integer.class, example = "10")
    @Min(1)
    @Max(100)
    @Builder.Default
    protected Integer size = 10;

    @Schema(description = "정렬 방법", implementation = Sort.Direction.class)
    @Builder.Default
    protected Sort.Direction sortDirection = Sort.Direction.DESC;
    @Builder.Default
    protected PaginationSortType field = PaginationSortType.ID;

    public Pageable toPageable() {
        return PageRequest.of(page, size, sortDirection, field.getField());
    }
}

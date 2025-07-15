package kr.co.amateurs.server.domain.dto.directmessage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class DirectMessageSearchPaginationParam extends PaginationParam {
    @Schema(description = "검색어", example = "안녕하세요")
    @NotNull
    private String keyword;

    public DirectMessageSearchPaginationParam() {
        super(0, 30, Sort.Direction.DESC, PaginationSortType.DM_SENT_AT);
    }
}

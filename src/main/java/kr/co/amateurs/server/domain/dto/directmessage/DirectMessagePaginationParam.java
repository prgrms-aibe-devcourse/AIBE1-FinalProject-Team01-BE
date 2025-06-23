package kr.co.amateurs.server.domain.dto.directmessage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import lombok.*;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DirectMessagePaginationParam extends PaginationParam {
    @Schema(description = "방 ID", example = "68550e24c95b3db2bc2deb36")
    @NotNull
    private String roomId;

    @Schema(description = "사용자 ID", example = "1")
    @NotNull
    private Long userId;

    public DirectMessagePaginationParam() {
        super(0, 30, Sort.Direction.DESC, PaginationSortType.DM_SENT_AT);
    }
}

package kr.co.amateurs.server.domain.dto.directmessage;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class DirectMessagePaginationParam extends PaginationParam {
    @Schema(description = "방 ID", example = "68550e24c95b3db2bc2deb36")
    private String roomId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    public DirectMessagePaginationParam() {
        super(0, 30, Sort.Direction.DESC, PaginationSortType.DM_SENT_AT);
    }
}

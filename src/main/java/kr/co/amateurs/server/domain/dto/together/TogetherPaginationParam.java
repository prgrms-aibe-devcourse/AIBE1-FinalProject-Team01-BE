package kr.co.amateurs.server.domain.dto.together;

import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class TogetherPaginationParam extends PaginationParam {
    private String keyword;
    private BoardType type;

    public TogetherPaginationParam() {
        super(0, 10, Sort.Direction.DESC, PaginationSortType.ID);
    }
}

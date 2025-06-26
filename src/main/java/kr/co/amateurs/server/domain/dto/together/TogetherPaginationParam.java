package kr.co.amateurs.server.domain.dto.together;


import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.query.SortDirection;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class TogetherPaginationParam extends PaginationParam {
    private String keyword = "";
}

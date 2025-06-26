package kr.co.amateurs.server.domain.dto.common;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class PostPaginationParam extends PaginationParam {
    private String keyword = "";
}

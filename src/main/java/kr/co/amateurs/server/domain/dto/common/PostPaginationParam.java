package kr.co.amateurs.server.domain.dto.common;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostPaginationParam extends PaginationParam {
    @Builder.Default
    private String keyword = "";
}

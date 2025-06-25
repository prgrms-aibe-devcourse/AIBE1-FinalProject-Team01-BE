package kr.co.amateurs.server.domain.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class ProjectSearchParam extends PaginationParam {
    @Schema(description = "검색 키워드", example = "Spring")
    private String keyword;
    @Schema(description = "개발 과정 트랙", example = "AI_BACKEND")
    private DevCourseTrack course;
    @Schema(description = "개발 과정 기수", example = "1")
    private String batch;

    public ProjectSearchParam() {
        super(0, 10, Sort.Direction.DESC, PaginationSortType.ID);
    }
}

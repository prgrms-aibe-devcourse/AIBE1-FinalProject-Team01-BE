package kr.co.amateurs.server.controller.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project", description = "프로젝트 허브 게시판 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "프로젝트 글 리스트", description = "프로젝트 허브 게시판의 글 목록을 불러옵니다.")
    public ResponseEntity<PageResponseDTO<ProjectResponseDTO>> getProjects(@ParameterObject @Valid ProjectSearchParam params) {
        PageResponseDTO<ProjectResponseDTO> projectPageResponseDTO = projectService.getProjects(params);
        return ResponseEntity.ok(projectPageResponseDTO);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 글 정보", description = "특정 프로젝트 글의 세부 정보를 불러옵니다.")
    public ResponseEntity<ProjectResponseDTO> getProjectDetails(@PathVariable(name = "projectId") Long projectId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        ProjectResponseDTO projectResponseDTO = projectService.getProjectDetails(projectId, ipAddress);
        return ResponseEntity.ok(projectResponseDTO);
    }

    // TODO: AI 구현 완료 시 추천 API 구현 예정
//    @GetMapping("/recommended")
//    public ResponseEntity<?> getRecommendedProjects() {
//
//        return ResponseEntity.ok("");
//    }

    @PostMapping
    @Operation(summary = "프로젝트 글쓰기", description = "프로젝트 허브 게시판의 새 글을 생성합니다.")
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody @Valid ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.createProject(projectRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDTO);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "프로젝트 글 수정", description = "프로젝트 허브 게시판의 특정 글을 수정합니다.")
    public ResponseEntity<Void> updateProject(@PathVariable Long projectId,
                                              @RequestBody @Valid ProjectRequestDTO projectRequestDTO) {
        projectService.updateProject(projectId, projectRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 글 수정", description = "프로젝트 허브 게시판의 특정 글을 삭제합니다.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}

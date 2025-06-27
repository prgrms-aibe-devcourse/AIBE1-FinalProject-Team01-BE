package kr.co.amateurs.server.controller.project;

import jakarta.validation.Valid;
import kr.co.amateurs.server.annotation.boardaccess.BoardAccess;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.project.ProjectPageResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectResponseDTO;
import kr.co.amateurs.server.domain.dto.project.ProjectSearchParam;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ProjectPageResponseDTO> getProjects(@Valid ProjectSearchParam params) {
        ProjectPageResponseDTO projectPageResponseDTO = projectService.getProjects(params);
        return ResponseEntity.ok(projectPageResponseDTO);
    }

    @GetMapping("/{postId}")
    @BoardAccess(needCategory = true, category = BoardCategory.PROJECT, hasPostId = true)
    public ResponseEntity<?> getProjectDetails(@PathVariable(name = "postId") Long postId) {
        ProjectResponseDTO projectResponseDTO = projectService.getProjectDetails(postId);
        return ResponseEntity.ok(projectResponseDTO);
    }

    // TODO: AI 구현 완료 시 추천 API 구현 예정
//    @GetMapping("/recommended")
//    public ResponseEntity<?> getRecommendedProjects() {
//
//        return ResponseEntity.ok("");
//    }

    @PostMapping
    @BoardAccess(needCategory = true, category = BoardCategory.PROJECT, operation = OperationType.WRITE,
            boardType = BoardType.PROJECT_HUB, hasBoardType = false)
    public ResponseEntity<?> createProject(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.createProject(userDetails.getUsername(), projectRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDTO);
    }

    @PutMapping("/{postId}")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE,
            boardType = BoardType.PROJECT_HUB, hasBoardType = false)
    public ResponseEntity<Void> updateProject(@PathVariable Long postId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody ProjectRequestDTO projectRequestDTO) {
        projectService.updateProject(userDetails.getUsername(), postId, projectRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    @BoardAccess(hasPostId = true, checkAuthor = true, operation = OperationType.WRITE,
            boardType = BoardType.PROJECT_HUB, hasBoardType = false)
    public ResponseEntity<Void> deleteProject(@PathVariable Long postId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.deleteProject(userDetails.getUsername(), postId);
        return ResponseEntity.noContent().build();
    }
}

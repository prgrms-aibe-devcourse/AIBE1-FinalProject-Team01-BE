package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.dto.project.ProjectRequestDTO;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime endedAt;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private String githubUrl;

    @Setter(AccessLevel.PRIVATE)
    private String simpleContent;

    @Setter(AccessLevel.PRIVATE)
    private String demoUrl;

    @Lob
    @Column(columnDefinition = "JSON")
    @Setter(AccessLevel.PRIVATE)
    private String projectMembers;

    public void update(ProjectRequestDTO projectRequestDTO) {
        Optional.ofNullable(projectRequestDTO.startedAt()).ifPresent(this::setStartedAt);
        Optional.ofNullable(projectRequestDTO.endedAt()).ifPresent(this::setEndedAt);
        Optional.ofNullable(projectRequestDTO.githubUrl()).ifPresent(this::setGithubUrl);
        Optional.ofNullable(projectRequestDTO.simpleContent()).ifPresent(this::setSimpleContent);
        Optional.ofNullable(projectRequestDTO.demoUrl()).ifPresent(this::setDemoUrl);
    }

    // Json 문자열로 저장하기 위해 메서드 분리
    public void updateProjectMembers(String projectMembers){
        this.projectMembers = projectMembers;
    }
}

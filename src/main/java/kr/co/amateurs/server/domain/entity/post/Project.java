package kr.co.amateurs.server.domain.entity.post;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(nullable = false)
    private String devcourseTrack;

    @Column(nullable = false)
    private Integer devcourseBatch;

    @Column(nullable = false)
    private String githubUrl;

    private String simpleContent;
    private String demoUrl;

    @Lob
    @Column(columnDefinition = "JSON")
    private String projectMembers;
}

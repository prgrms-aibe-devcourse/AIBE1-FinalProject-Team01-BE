package kr.co.amateurs.server.domain.entity.verify;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.dto.verify.PythonServiceResponseDTO;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verifies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class Verify extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerifyStatus status;

    @Column(nullable = false)
    private Integer ocrScore;

    @Column(nullable = false)
    private Integer layoutScore;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(columnDefinition = "TEXT")
    private String detailMessage;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime completedAt;
} 
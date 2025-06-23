package kr.co.amateurs.server.domain.entity.report;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.comment.Comment;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;


    public static Report fromPost(Post post, User user, String description) {
        return Report.builder()
                .user(user)
                .post(post)
                .description(description)
                .status(ReportStatus.PENDING)
                .build();
    }

    public static Report fromComment(Comment comment, User user, String description) {
        return Report.builder()
                .user(user)
                .comment(comment)
                .description(description)
                .status(ReportStatus.PENDING)
                .build();
    }

    public void updateStatusReport(ReportStatus status) {
        this.status = status;
    }
}

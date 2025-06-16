package kr.co.amateurs.server.domain.entity.topic;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.user.User;
import lombok.*;

@Entity
@Table(name = "user_topics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserTopic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Topic topic;
}

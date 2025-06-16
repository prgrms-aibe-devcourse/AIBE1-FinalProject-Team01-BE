package kr.co.amateurs.server.domain.entity.user;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.common.TimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

    @Column(name = "devcourse_name", nullable = false)
    private String devcourseName;

    @Column(nullable = false)
    private String devcourseBatch;

    private String imageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<UserTopic> userTopics = new ArrayList<>();
}

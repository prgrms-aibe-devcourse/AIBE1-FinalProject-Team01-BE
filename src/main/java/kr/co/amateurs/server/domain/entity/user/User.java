package kr.co.amateurs.server.domain.entity.user;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String providerId;
    @Enumerated(EnumType.STRING)
    private DevCourseTrack devcourseName;
    private String password;
    private String devcourseBatch;
    private String imageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserTopic> userTopics = new ArrayList<>();

    public void addUserTopics(Set<Topic> topics) {
        this.userTopics.clear();

        topics.forEach(topic -> {
            UserTopic userTopic = UserTopic.builder()
                    .user(this)
                    .topic(topic)
                    .build();
            this.userTopics.add(userTopic);
        });
    }

    public void updateBasicProfile(String name, String nickname, String imageUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void updatePassword(String password) {
        if(password != null && !password.trim().isEmpty()) {
            this.password = password;
        }
    }
}

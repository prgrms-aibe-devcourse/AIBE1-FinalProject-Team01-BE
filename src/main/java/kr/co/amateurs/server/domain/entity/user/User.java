package kr.co.amateurs.server.domain.entity.user;

import jakarta.persistence.*;
import kr.co.amateurs.server.domain.entity.common.BaseEntity;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import lombok.*;

import java.time.LocalDateTime;
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

    @Builder.Default
    @Column(name = "is_profile_completed", nullable = false)
    private Boolean isProfileCompleted = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


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

    public boolean isProfileCompleted() {
        return this.isProfileCompleted != null && this.isProfileCompleted;
    }

    public void completeProfile(String name, String nickname, Set<Topic> topics) {
        updateBasicProfile(name, nickname, this.imageUrl);
        addUserTopics(topics);
        this.isProfileCompleted = true;
    }

    public void markProfileAsCompleted() {
        this.isProfileCompleted = true;
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

    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted;
    }

    public void anonymizeAndDelete(String anonymousEmail, String anonymousNickname) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();

        this.email = anonymousEmail;
        this.nickname = anonymousNickname;
        this.name = "탈퇴한회원";

        this.imageUrl = null;
        this.providerId = null;
        this.devcourseBatch = null;

        this.userTopics.clear();
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }
}

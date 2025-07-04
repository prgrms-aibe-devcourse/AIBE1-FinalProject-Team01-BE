package kr.co.amateurs.server.repository.user;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderIdAndProviderType(String providerId, ProviderType providerType);

    Optional<User> findByNickname(String testUser);

    @Query("SELECT ut.topic FROM UserTopic ut WHERE ut.user.id = :userId")
    List<Topic> findTopicDisplayNamesByUserId(@Param("userId") Long userId);

    Page<User> findByRoleIn(List<Role> roles, Pageable pageable);
}

package kr.co.amateurs.server.repository.user;

import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String testUser);
}

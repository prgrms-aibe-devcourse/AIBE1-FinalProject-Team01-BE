package kr.co.amateurs.server.repository.user;

import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

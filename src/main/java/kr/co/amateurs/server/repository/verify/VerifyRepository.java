package kr.co.amateurs.server.repository.verify;

import kr.co.amateurs.server.domain.entity.verify.Verify;
import kr.co.amateurs.server.domain.entity.verify.VerifyStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VerifyRepository extends JpaRepository<Verify, Long> {
    List<Verify> findByUser(User user);
    boolean existsByUserAndStatusIn(User user, List<VerifyStatus> statuses);
    Optional<Verify> findFirstByUserOrderByCreatedAtDesc(User user);
} 
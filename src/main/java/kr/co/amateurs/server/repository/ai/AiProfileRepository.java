package kr.co.amateurs.server.repository.ai;

import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiProfileRepository extends JpaRepository<AiProfile, Long> {
    Optional<AiProfile> findByUserId(Long userId);

    boolean existsByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime since);
}

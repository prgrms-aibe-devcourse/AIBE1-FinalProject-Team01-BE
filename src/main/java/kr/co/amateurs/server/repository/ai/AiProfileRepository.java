package kr.co.amateurs.server.repository.ai;

import kr.co.amateurs.server.domain.entity.ai.AiProfile;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiProfileRepository extends JpaRepository<AiProfile, Long> {

    // 사용자별 프로필 조회
    Optional<AiProfile> findByUser(User user);

    Optional<AiProfile> findByUserId(Long userId);

    // 프로필이 없는 사용자 조회 (EXISTS 서브쿼리 사용)
    @Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT 1 FROM AiProfile ap WHERE ap.user = u)")
    List<User> findUsersWithoutProfile();

    // 특정 키워드를 포함하는 프로필 조회 (분석용)
    @Query("SELECT ap FROM AiProfile ap WHERE ap.interestKeywords LIKE %:keyword%")
    List<AiProfile> findByInterestKeywordsContaining(@Param("keyword") String keyword);

    // 사용자 ID로 프로필 존재 여부 확인
    boolean existsByUserId(Long userId);

    // 사용자로 프로필 존재 여부 확인
    boolean existsByUser(User user);
}

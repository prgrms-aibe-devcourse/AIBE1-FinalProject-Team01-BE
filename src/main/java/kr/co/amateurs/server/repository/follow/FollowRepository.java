package kr.co.amateurs.server.repository.follow;

import kr.co.amateurs.server.domain.entity.follow.Follow;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByToUser(User toUser);

    List<Follow> findByFromUser(User fromUser);

    void deleteByToUserAndFromUser(User toUser, User fromUser);

    boolean existsByFromUserAndToUser(User fromUser, User toUser);
}

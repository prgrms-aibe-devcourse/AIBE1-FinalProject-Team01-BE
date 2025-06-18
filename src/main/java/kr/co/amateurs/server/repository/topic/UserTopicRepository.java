package kr.co.amateurs.server.repository.topic;

import kr.co.amateurs.server.domain.entity.topic.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
}

package kr.co.amateurs.server.repository.directmessage;

import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRoomRepository extends MongoRepository<DirectMessageRoom, String> {
    @Query("{'participants.userId': {$all: [?0, ?1]}, 'participants': {$size: 2}}")
    Optional<DirectMessageRoom> findRoomByUserIds(Long userId1, Long userId2);

    @Query("{'participants': {$elemMatch: {'userId': ?0, 'isActive': true}}}")
    List<DirectMessageRoom> findActiveRoomsByUserId(Long userId);
    
    @Query("{'participants.userId': ?0}")
    List<DirectMessageRoom> findAllRoomsByUserId(Long userId);
}

package kr.co.amateurs.server.repository.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface AlarmRepository extends MongoRepository<Alarm, String> {
    @Modifying
    @Query("{ 'userId': ?0, 'isRead': false }")
    @Update("{ '$set': { 'isRead': true } }")
    void markAllAsReadByUserId(long userId);

    Page<Alarm> findByUserId(long userId, Pageable pageable);
}

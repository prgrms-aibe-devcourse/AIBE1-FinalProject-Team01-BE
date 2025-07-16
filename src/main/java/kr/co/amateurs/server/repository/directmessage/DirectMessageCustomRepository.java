package kr.co.amateurs.server.repository.directmessage;

import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageSearchPaginationParam;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessageRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DM 검색 Repository - 2단계로 깔끔하게 처리
 */
@Repository
@RequiredArgsConstructor
public class DirectMessageCustomRepository {
    
    private final MongoTemplate mongoTemplate;

    public Page<DirectMessage> searchMessages(List<DirectMessageRoom> userRooms, Long userId, DirectMessageSearchPaginationParam param) {
        
        if (userRooms.isEmpty()) {
            return new PageImpl<>(List.of(), param.toPageable(), 0);
        }
        
        List<Criteria> orConditions = new ArrayList<>();
        
        for (DirectMessageRoom room : userRooms) {
            LocalDateTime reEntryAt = room.getParticipantReEntryAt(userId);
            
            Criteria roomCriteria = Criteria.where("roomId").is(room.getId())
                .and("content").regex(param.getKeyword(), "i");
            
            if (reEntryAt != null) {
                roomCriteria = roomCriteria.and("sentAt").gte(reEntryAt);
            }
            
            orConditions.add(roomCriteria);
        }
        
        Criteria finalCriteria = new Criteria().orOperator(orConditions.toArray(new Criteria[0]));
        return executeSearch(finalCriteria, param.toPageable());
    }
    
    private Page<DirectMessage> executeSearch(Criteria criteria, Pageable pageable) {
        List<AggregationOperation> operations = List.of(
            Aggregation.match(criteria),
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "sentAt")),
            Aggregation.skip(pageable.getOffset()),
            Aggregation.limit(pageable.getPageSize())
        );
        
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<DirectMessage> results = mongoTemplate.aggregate(
            aggregation, "direct_messages", DirectMessage.class
        ).getMappedResults();
        
        long total = mongoTemplate.count(new Query(criteria), DirectMessage.class);
        return new PageImpl<>(results, pageable, total);
    }
}

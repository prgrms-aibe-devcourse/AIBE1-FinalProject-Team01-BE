package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.dto.alarm.AlarmPageResponse;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public AlarmPageResponse readAlarms(long userId, PaginationParam param) {
        Page<Alarm> page = alarmRepository.findByUserId(userId, param.toPageable());
        return AlarmPageResponse.from(page);
    }

    public void markAllAsRead(long userId) {
        alarmRepository.markAllAsReadByUserId(userId);
    }

    public void markAsRead(long userId, String alarmId) {
        alarmRepository.markAsReadByUserIdAndId(userId, alarmId);
    }
}

package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public void readAll(long userId) {
        alarmRepository.markAllAsReadByUserId(userId);
    }
}

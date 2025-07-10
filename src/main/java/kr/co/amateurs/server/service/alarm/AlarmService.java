package kr.co.amateurs.server.service.alarm;

import kr.co.amateurs.server.domain.dto.alarm.AlarmPageDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    private final UserService userService;
    private final SseService sseService;

    public void saveAlarm(Alarm alarm) {
        Alarm savedAlarm = alarmRepository.save(alarm);
        sseService.sendAlarmToUser(savedAlarm.getUserId(), savedAlarm);
    }

    public void createTestAlarm() {
        Alarm alarm = Alarm.builder()
                .userId(userService.getCurrentLoginUser().getId())
                .type(AlarmType.COMMENT)
                .title("테스트용 더미 알람 데이터")
                .content("개발개밥계발")
                .build();
        alarmRepository.save(alarm);
    }

    public AlarmPageDTO readAlarms(PaginationParam param) {
        User user = userService.getCurrentLoginUser();
        Page<Alarm> page = alarmRepository.findByUserId(user.getId(), param.toPageable());
        long unReadCount = alarmRepository.countByUserIdAndIsReadFalse(user.getId());
        return AlarmPageDTO.from(page, unReadCount);
    }

    public void markAllAsRead() {
        User user = userService.getCurrentLoginUser();
        alarmRepository.markAllAsReadByUserId(user.getId());
    }

    public void markAsRead(String alarmId) {
        User user = userService.getCurrentLoginUser();
        alarmRepository.markAsReadByUserIdAndId(user.getId(), alarmId);
    }
}

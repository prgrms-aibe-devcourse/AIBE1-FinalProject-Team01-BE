package kr.co.amateurs.server.domain.dto.alarm;

import kr.co.amateurs.server.domain.dto.common.PageInfo;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import org.springframework.data.domain.Page;

import java.util.List;

public record AlarmPageResponse(
        List<AlarmResponse> alarms,
        PageInfo pageInfo
) {
    public static AlarmPageResponse from(Page<Alarm> page) {
        List<AlarmResponse> responses = page.getContent().stream()
                .map(AlarmResponse::from)
                .toList();
        PageInfo info = PageInfo.from(page);
        return new AlarmPageResponse(responses, info);
    }
}

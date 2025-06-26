package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AlarmCreatorRegistry {

    private final Map<AlarmType, AlarmCreator> processorMap;

    public AlarmCreatorRegistry(List<AlarmCreator> creators) {
        this.processorMap = creators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AlarmCreator::getType,
                        Function.identity()
                ));
    }

    public AlarmCreator getCreator(AlarmType type) {
        return Optional.ofNullable(processorMap.get(type))
                .orElseThrow(ErrorCode.ILLEGAL_ALARM_CREATOR);
    }
}

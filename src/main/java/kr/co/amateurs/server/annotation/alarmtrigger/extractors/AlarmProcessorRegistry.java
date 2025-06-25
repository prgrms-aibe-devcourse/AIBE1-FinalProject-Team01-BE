package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AlarmProcessorRegistry {

    private final Map<AlarmReceiver, AlarmProcesser> extractorMap;

    public AlarmProcessorRegistry(List<AlarmProcesser> extractors) {
        this.extractorMap = extractors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AlarmProcesser::getReceiver,
                        Function.identity()
                ));
    }

    public AlarmProcesser getExtractor(AlarmReceiver receiver) {
        return Optional.ofNullable(extractorMap.get(receiver))
                .orElseThrow(ErrorCode.ILLEGAL_ALARM_EXTRACTOR);
    }
}

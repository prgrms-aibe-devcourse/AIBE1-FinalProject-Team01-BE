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

    private final Map<AlarmReceiver, AlarmProcessor> processorMap;

    public AlarmProcessorRegistry(List<AlarmProcessor> processors) {
        this.processorMap = processors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AlarmProcessor::getReceiver,
                        Function.identity()
                ));
    }

    public AlarmProcessor getProcessor(AlarmReceiver receiver) {
        return Optional.ofNullable(processorMap.get(receiver))
                .orElseThrow(ErrorCode.ILLEGAL_ALARM_EXTRACTOR);
    }
}

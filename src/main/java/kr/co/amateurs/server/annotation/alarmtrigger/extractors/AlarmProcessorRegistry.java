package kr.co.amateurs.server.annotation.alarmtrigger.extractors;

import kr.co.amateurs.server.domain.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알람 프로세서들을 관리하는 레지스트리 클래스입니다.
 * 
 * Registry Pattern을 적용하여 AlarmReceiver 타입별로 적절한 
 * AlarmProcessor 구현체를 매핑하고 조회할 수 있습니다.
 * 
 * Spring 컨테이너에서 모든 AlarmProcessor 구현체를 자동으로 주입받아
 * 각 프로세서의 getReceiver() 메서드 반환값을 키로 하는 맵을 구성합니다.
 */
@Component
public class AlarmProcessorRegistry {

    private final Map<AlarmReceiver, AlarmProcessor> processorMap;

    /**
     * 모든 AlarmProcessor 구현체들을 주입받아 레지스트리를 초기화합니다.
     * 
     * @param processors Spring 컨테이너에서 주입받은 모든 AlarmProcessor 구현체 목록
     */
    public AlarmProcessorRegistry(List<AlarmProcessor> processors) {
        this.processorMap = processors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AlarmProcessor::getReceiver,
                        Function.identity()
                ));
    }

    /**
     * 주어진 AlarmReceiver 타입에 맞는 프로세서를 조회합니다.
     * 
     * @param receiver 알람 수신자 타입
     * @return 해당 타입을 처리할 수 있는 AlarmProcessor
     * @throws RuntimeException 등록되지 않은 수신자 타입인 경우
     */
    public AlarmProcessor getProcessor(AlarmReceiver receiver) {
        return Optional.ofNullable(processorMap.get(receiver))
                .orElseThrow(ErrorCode.ILLEGAL_ALARM_EXTRACTOR);
    }
}

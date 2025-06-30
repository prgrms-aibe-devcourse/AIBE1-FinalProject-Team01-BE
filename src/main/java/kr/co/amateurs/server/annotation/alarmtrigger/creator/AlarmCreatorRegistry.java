package kr.co.amateurs.server.annotation.alarmtrigger.creator;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알람 생성자들을 관리하는 레지스트리 클래스입니다.
 * 
 * Registry Pattern을 적용하여 AlarmType별로 적절한 
 * AlarmCreator 구현체를 매핑하고 조회할 수 있습니다.
 * 
 * Spring 컨테이너에서 모든 AlarmCreator 구현체를 자동으로 주입받아
 * 각 생성자의 getType() 메서드 반환값을 키로 하는 불변 맵을 구성합니다.
 * 
 * 사용 흐름:
 * 1. AlarmTrigger 어노테이션에서 AlarmType 추출
 * 2. 해당 타입에 맞는 AlarmCreator 조회
 * 3. 조회된 생성자로 알람 생성 처리
 */
@Component
public class AlarmCreatorRegistry {

    private final Map<AlarmType, AlarmCreator> processorMap;

    /**
     * 모든 AlarmCreator 구현체들을 주입받아 레지스트리를 초기화합니다.
     * 
     * 각 생성자의 getType() 반환값을 키로 하는 불변 맵을 생성하며,
     * 동일한 타입을 가진 생성자가 여러 개 있을 경우 예외가 발생합니다.
     * 
     * @param creators Spring 컨테이너에서 주입받은 모든 AlarmCreator 구현체 목록
     * @throws IllegalStateException 중복된 알람 타입이 존재하는 경우
     */
    public AlarmCreatorRegistry(List<AlarmCreator> creators) {
        this.processorMap = creators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AlarmCreator::getType,
                        Function.identity()
                ));
    }

    /**
     * 주어진 AlarmType에 맞는 알람 생성자를 조회합니다.
     * 
     * @param type 조회할 알람 타입
     * @return 해당 타입을 처리할 수 있는 AlarmCreator 구현체
     * @throws CustomException 등록되지 않은 알람 타입인 경우
     */
    public AlarmCreator getCreator(AlarmType type) {
        return Optional.ofNullable(processorMap.get(type))
                .orElseThrow(ErrorCode.ILLEGAL_ALARM_CREATOR);
    }
}

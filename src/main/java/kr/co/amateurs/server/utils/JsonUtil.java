package kr.co.amateurs.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtil {
    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON 문자열로 변환
     *
     * @param object 변환할 객체
     * @return JSON 문자열 (null이면 null 반환)
     * @throws RuntimeException JSON 변환 실패 시
     */
    public <T> String toJson(T object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패 - 객체 타입: {}", object.getClass().getSimpleName(), e);
            throw new RuntimeException("JSON 변환 실패: " + object.getClass().getSimpleName(), e);
        }
    }

    /**
     * List를 JSON 문자열로 변환
     *
     * @param list 변환할 리스트
     * @return JSON 문자열 (null이나 빈 리스트면 null 반환)
     * @throws RuntimeException JSON 변환 실패 시
     */
    public <T> String listToJson(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("List JSON 변환 실패 - 리스트 크기: {}", list.size(), e);
            throw new RuntimeException("List JSON 변환 실패", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환
     *
     * @param json JSON 문자열
     * @param clazz 대상 클래스
     * @return 변환된 객체 (null이나 빈 문자열이면 null 반환)
     * @throws RuntimeException JSON 파싱 실패 시
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패 - 타입: {}, 데이터: {}", clazz.getSimpleName(), json, e);
            throw new RuntimeException("JSON 파싱 실패: " + clazz.getSimpleName(), e);
        }
    }

    /**
     * JSON 문자열을 List로 변환
     *
     * @param json JSON 문자열
     * @param typeRef TypeReference
     * @return 변환된 리스트 (null이나 빈 문자열이면 빈 리스트 반환)
     * @throws RuntimeException JSON 파싱 실패 시
     */
    public <T> List<T> fromJsonToList(String json, TypeReference<List<T>> typeRef) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<T> result = objectMapper.readValue(json, typeRef);
            return result != null ? result : Collections.emptyList();
        } catch (JsonProcessingException e) {
            log.error("JSON List 파싱 실패 - 데이터: {}", json, e);
            throw new RuntimeException("JSON List 파싱 실패", e);
        }
    }
}

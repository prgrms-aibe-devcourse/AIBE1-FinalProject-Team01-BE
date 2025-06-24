package kr.co.amateurs.server.controller.directmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.SecurityConfig;
import kr.co.amateurs.server.domain.dto.common.PageInfo;
import kr.co.amateurs.server.domain.dto.directmessage.*;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DirectMessageController.class)
@Import(SecurityConfig.class)
public class DirectMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectMessageService directMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class 채팅방_생성 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅방_생성_요청_시_201_응답과_방_정보가_반환() throws Exception {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2"))
                        .build();

                DirectMessageRoomResponse response = DirectMessageRoomResponse.builder()
                        .roomId("room123")
                        .lastMessage(null)
                        .otherUserId(2L)
                        .build();

                given(directMessageService.createRoom(any(DirectMessageRoomCreateRequest.class)))
                        .willReturn(response);

                // when & then
                mockMvc.perform(post("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.roomId").value("room123"))
                        .andExpect(jsonPath("$.otherUserId").value(2))
                        .andExpect(jsonPath("$.lastMessage").isEmpty())
                        .andDo(print());
            }
        }

        @Nested
        class 실패 {
            @Test
            void 세명_이상의_참여자로_채팅방_생성_시_400_반환() throws Exception {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1", 2L, "user2", 3L, "user3"))
                        .build();

                // when & then
                mockMvc.perform(post("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }

            @Test
            void 한명만_있는_채팅방_생성_시_400_반환() throws Exception {
                // given
                DirectMessageRoomCreateRequest request = DirectMessageRoomCreateRequest.builder()
                        .participantMap(Map.of(1L, "user1"))
                        .build();

                // when & then
                mockMvc.perform(post("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }

            @Test
            void 빈_참여자_맵으로_채팅방_생성_시_400_반환() throws Exception {
                // given
                String invalidJson = "{\"participantMap\":{}}";

                // when & then
                mockMvc.perform(post("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }
    }

    @Nested
    class 채팅방_목록_조회 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅방_목록_조회_시_200과_방_목록_반환() throws Exception {
                // given
                Long userId = 1L;
                List<DirectMessageRoomResponse> rooms = List.of(
                        DirectMessageRoomResponse.builder()
                                .roomId("room1")
                                .lastMessage("안녕하세요")
                                .otherUserId(2L)
                                .build(),
                        DirectMessageRoomResponse.builder()
                                .roomId("room2")
                                .lastMessage("반갑습니다")
                                .otherUserId(3L)
                                .build()
                );

                given(directMessageService.getRooms(userId)).willReturn(rooms);

                // when & then
                mockMvc.perform(get("/api/v1/dm/rooms/{userId}", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].roomId").value("room1"))
                        .andExpect(jsonPath("$[0].lastMessage").value("안녕하세요"))
                        .andExpect(jsonPath("$[0].otherUserId").value(2))
                        .andExpect(jsonPath("$[1].roomId").value("room2"))
                        .andExpect(jsonPath("$[1].lastMessage").value("반갑습니다"))
                        .andExpect(jsonPath("$[1].otherUserId").value(3))
                        .andDo(print());
            }

            @Test
            void 채팅방이_없는_사용자_조회_시_빈_배열이_반환() throws Exception {
                // given
                Long userId = 999L;
                given(directMessageService.getRooms(userId)).willReturn(List.of());

                // when & then
                mockMvc.perform(get("/api/v1/dm/rooms/{userId}", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0))
                        .andDo(print());
            }
        }

        @Nested
        class 실패 {

            @Test
            void 잘못된_사용자_ID_형식으로_조회_시_400_반환() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/dm/rooms/{userId}", "invalid"))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }
    }

    @Nested
    class 채팅_기록_조회 {

        @Nested
        class 성공 {

            @Test
            void 정상적인_채팅_기록_조회_시_200_응답과_메시지_목록이_반환() throws Exception {
                // given
                String roomId = "room123";
                DirectMessageResponse message1 = DirectMessageResponse.builder()
                        .id("msg1")
                        .roomId(roomId)
                        .content("첫 번째 메시지")
                        .senderId(1L)
                        .senderNickname("user1")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now())
                        .build();
                DirectMessageResponse message2 = DirectMessageResponse.builder()
                        .id("msg2")
                        .roomId(roomId)
                        .content("두 번째 메시지")
                        .senderId(2L)
                        .senderNickname("user2")
                        .messageType(MessageType.TEXT)
                        .sentAt(LocalDateTime.now())
                        .build();
                List<DirectMessageResponse> messages = List.of(message1, message2);

                DirectMessagePageResponse pageResponse = new DirectMessagePageResponse(
                        messages,
                        new PageInfo(0, 1, 1, 2L)
                );

                given(directMessageService.getMessages(any(DirectMessagePaginationParam.class)))
                        .willReturn(pageResponse);

                // when & then
                mockMvc.perform(get("/api/v1/dm/messages")
                                .param("roomId", "room123")
                                .param("userId", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.messages.length()").value(2))
                        .andExpect(jsonPath("$.messages[0].id").value(message1.id()))
                        .andExpect(jsonPath("$.messages[0].content").value(message1.content()))
                        .andExpect(jsonPath("$.messages[0].senderId").value(message1.senderId()))
                        .andExpect(jsonPath("$.messages[0].senderNickname").value(message1.senderNickname()))
                        .andExpect(jsonPath("$.messages[1].id").value(message2.id()))
                        .andExpect(jsonPath("$.messages[1].content").value(message2.content()))
                        .andExpect(jsonPath("$.pageInfo.pageNumber").value(pageResponse.pageInfo().getPageNumber()))
                        .andExpect(jsonPath("$.pageInfo.totalElements").value(pageResponse.pageInfo()
                                .getTotalElements()))
                        .andDo(print());
            }

            @Test
            void 메시지가_없는_채팅방_조회_시_빈_메시지_목록_반환() throws Exception {
                // given
                DirectMessagePageResponse pageResponse = new DirectMessagePageResponse(
                        List.of(),
                        new PageInfo(0, 0, 1, 0L)
                );

                given(directMessageService.getMessages(any(DirectMessagePaginationParam.class)))
                        .willReturn(pageResponse);

                // when & then
                mockMvc.perform(get("/api/v1/dm/messages")
                                .param("roomId", "room123")
                                .param("userId", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.messages.length()").value(0))
                        .andExpect(jsonPath("$.pageInfo.totalElements").value(0))
                        .andDo(print());
            }

            @Test
            void 페이지네이션_파라미터_테스트() throws Exception {
                // given
                DirectMessagePageResponse pageResponse = new DirectMessagePageResponse(
                        List.of(),
                        new PageInfo(1, 10, 3, 25L)
                );

                given(directMessageService.getMessages(any(DirectMessagePaginationParam.class)))
                        .willReturn(pageResponse);

                // when & then
                mockMvc.perform(get("/api/v1/dm/messages")
                                .param("roomId", "room123")
                                .param("userId", "1")
                                .param("page", "1")
                                .param("size", "10"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.pageInfo.pageNumber").value(1))
                        .andExpect(jsonPath("$.pageInfo.pageSize").value(10))
                        .andExpect(jsonPath("$.pageInfo.totalPages").value(3))
                        .andExpect(jsonPath("$.pageInfo.totalElements").value(25))
                        .andDo(print());
            }
        }

        @Nested
        class 실패 {

            @Test
            void 필수_파라미터_누락_시_400_반환() throws Exception {
                // given
                DirectMessagePageResponse pageResponse = new DirectMessagePageResponse(
                        List.of(),
                        new PageInfo(0, 0, 1, 0L)
                );

                given(directMessageService.getMessages(any(DirectMessagePaginationParam.class)))
                        .willReturn(pageResponse);

                // when & then
                mockMvc.perform(get("/api/v1/dm/messages")
                                .param("userId", "1")
                                .param("size", "10"))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }
    }

    @Nested
    class 채팅방_나가기 {

        @Nested
        class 성공 {

            @Test
            @DisplayName("정상적인 방 나가기 요청 시 204 응답이 반환되어야 한다")
            void 정상적인_방_나가기_요청_시_204_응답이_반환되어야_한다() throws Exception {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest(
                        "room123",
                        1L
                );

                doNothing().when(directMessageService).exitRoom(any(DirectMessageRoomExitRequest.class));

                // when & then
                mockMvc.perform(delete("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNoContent())
                        .andDo(print());
            }
        }

        @Nested
        class 실패 {

            @Test
            @DisplayName("빈 roomId로 방 나가기 시 400 에러가 발생해야 한다")
            void 빈_roomId로_방_나가기_시_400_에러가_발생해야_한다() throws Exception {
                // given
                DirectMessageRoomExitRequest request = new DirectMessageRoomExitRequest(
                        "",
                        1L
                );

                // when & then
                mockMvc.perform(delete("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }

            @Test
            @DisplayName("null userId로 방 나가기 시 400 에러가 발생해야 한다")
            void null_userId로_방_나가기_시_400_에러가_발생해야_한다() throws Exception {
                // given
                String requestJson = "{\"roomId\":\"room123\",\"userId\":null}";

                // when & then
                mockMvc.perform(delete("/api/v1/dm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
        }
    }
}

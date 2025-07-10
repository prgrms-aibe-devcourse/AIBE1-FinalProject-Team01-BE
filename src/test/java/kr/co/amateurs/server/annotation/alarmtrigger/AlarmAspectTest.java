package kr.co.amateurs.server.annotation.alarmtrigger;

import kr.co.amateurs.server.config.jwt.CustomUserDetailsService;
import kr.co.amateurs.server.controller.common.AbstractControllerTest;
import kr.co.amateurs.server.domain.dto.directmessage.DirectMessageRequest;
import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import kr.co.amateurs.server.domain.entity.directmessage.enums.MessageType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.fixture.directmessage.DirectMessageRoomFixture;
import kr.co.amateurs.server.fixture.project.UserFixture;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import kr.co.amateurs.server.repository.directmessage.DirectMessageRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.alarm.SseService;
import kr.co.amateurs.server.service.directmessage.DirectMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AlarmAspectTest extends AbstractControllerTest {

    @Autowired
    private DirectMessageService directMessageService;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private SseService sseService;

    @Autowired
    private DirectMessageRoomFixture directMessageRoomFixture;

    private User messageReceiver;
    private User messageSender;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        alarmRepository.deleteAll();

        messageReceiver = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "3")
                .toBuilder()
                .email("receiver@test.com")
                .nickname("메시지수신자")
                .build();
        messageReceiver = userRepository.save(messageReceiver);

        messageSender = UserFixture.createStudentUser(DevCourseTrack.AI_BACKEND, "4")
                .toBuilder()
                .email("sender@test.com")
                .nickname("메시지발신자")
                .build();
        messageSender = userRepository.save(messageSender);
    }

    @Nested
    class AOP_알람_트리거_동작_검증 {

        @Test
        void 다이렉트_메시지_전송_시_수신자에게_알람이_자동으로_생성된다() {
            // given
            setAuthentication(messageReceiver.getEmail());
            sseService.connect();

            directMessageRoomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_1,
                    messageReceiver.getId(),
                    messageReceiver.getNickname(),
                    messageSender.getId(),
                    messageSender.getNickname()
            );

            DirectMessageRequest messageRequest = DirectMessageRequest.builder()
                    .content("AOP 테스트 메시지입니다!")
                    .senderId(messageSender.getId())
                    .senderName(messageSender.getNickname())
                    .messageType(MessageType.TEXT)
                    .build();

            // when
            directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_1, messageRequest);

            // then
            List<Alarm> alarms = alarmRepository.findAll();
            assertThat(alarms).hasSize(1);

            Alarm alarm = alarms.get(0);
            assertThat(alarm.getType()).isEqualTo(AlarmType.DIRECT_MESSAGE);
            assertThat(alarm.getTitle()).isEqualTo(AlarmType.DIRECT_MESSAGE.getTitle());
            assertThat(alarm.getContent()).contains(messageSender.getNickname() + "님으로부터 새로운 메시지가 도착했습니다.");
            assertThat(alarm.isRead()).isFalse();
        }
    }

    @Nested
    class AOP_트랜잭션_분리_검증 {

        @Test
        void 알람_생성_실패가_메인_비즈니스_로직에_영향을_주지_않는다() {
            // given
            directMessageRoomFixture.createAndSaveRoom(
                    DirectMessageRoomFixture.ROOM_1,
                    messageReceiver.getId(),
                    messageReceiver.getNickname(),
                    messageSender.getId(),
                    messageSender.getNickname()
            );

            // messageType is null
            DirectMessageRequest messageRequest = DirectMessageRequest.builder()
                    .content("트랜잭션 분리 테스트 메시지")
                    .senderId(messageSender.getId())
                    .senderName(messageSender.getNickname())
                    .build();

            // when
            assertThatThrownBy(() -> {
                directMessageService.saveMessage(DirectMessageRoomFixture.ROOM_1, messageRequest);
            }).isInstanceOf(RuntimeException.class);

            // then
            List<DirectMessage> messages = directMessageRepository.findAll();
            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getContent()).isEqualTo("트랜잭션 분리 테스트 메시지");

            List<Alarm> alarms = alarmRepository.findAll();
            assertThat(alarms).isEmpty();
        }
    }

    private void setAuthentication(String email) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

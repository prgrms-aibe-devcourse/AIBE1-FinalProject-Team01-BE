package kr.co.amateurs.server.fixture.alarm;

import kr.co.amateurs.server.domain.entity.alarm.Alarm;
import kr.co.amateurs.server.domain.entity.alarm.enums.AlarmType;
import kr.co.amateurs.server.domain.entity.alarm.metadata.CommentMetaData;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.fixture.common.TestConstants;
import kr.co.amateurs.server.repository.alarm.AlarmRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
public class AlarmTestFixture {

    private final AlarmRepository alarmRepository;

    public static final String COMMENT_TITLE = "새로운 댓글이 작성되었습니다.";
    public static final String REPLY_TITLE = "새로운 대댓글이 작성되었습니다.";
    public static final String DM_TITLE = "새로운 DM이 도착했습니다.";

    public static final String COMMENT_CONTENT = "회원님의 게시글에 새로운 댓글이 작성되었습니다.";
    public static final String REPLY_CONTENT = "회원님의 댓글에 새로운 대댓글이 작성되었습니다.";
    public static final String DM_CONTENT = "새로운 다이렉트 메시지가 도착했습니다.";

    public static final Long POST_ID = 123L;
    public static final Long COMMENT_ID = 456L;
    public static final BoardType DEFAULT_BOARD_TYPE = BoardType.FREE;

    public AlarmTestFixture(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

    public static Alarm.AlarmBuilder defaultAlarm() {
        return Alarm.builder()
                .userId(TestConstants.USER_ID_1)
                .type(AlarmType.COMMENT)
                .title(COMMENT_TITLE)
                .content(COMMENT_CONTENT)
                .metaData(createCommentMetaData())
                .isRead(false)
                .sentAt(LocalDateTime.now());
    }

    public static CommentMetaData createCommentMetaData() {
        return new CommentMetaData(POST_ID, DEFAULT_BOARD_TYPE, COMMENT_ID);
    }

    public static CommentMetaData createCommentMetaData(Long postId, BoardType boardType, Long commentId) {
        return new CommentMetaData(postId, boardType, commentId);
    }

    public void createMultipleAlarms(Long userId, int count) {
        for (int i = 1; i <= count; i++) {
            Alarm alarm = defaultAlarm()
                    .id("alarm" + userId + "_" + i)
                    .userId(userId)
                    .content(COMMENT_CONTENT + " " + i)
                    .sentAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            alarmRepository.save(alarm);
        }
    }

    public void createMixedReadStatusAlarms(long userId, int count) {
        List<Alarm> alarms = IntStream.range(0, count).boxed().map(i -> defaultAlarm()
                        .id("unread" + UUID.randomUUID())
                        .userId(userId)
                        .isRead(i % 2 == 0)
                        .content("test content" + i)
                        .sentAt(LocalDateTime.now())
                        .build())
                .toList();
        alarmRepository.saveAll(alarms);
    }

    public void createAlarmsWithDifferentTypes(Long userId) {
        alarmRepository.saveAll(List.of(
                defaultAlarm()
                        .id("comment_alarm_" + userId)
                        .userId(userId)
                        .type(AlarmType.COMMENT)
                        .title(COMMENT_TITLE)
                        .content(COMMENT_CONTENT)
                        .build(),
                defaultAlarm()
                        .id("reply_alarm_" + userId)
                        .userId(userId)
                        .type(AlarmType.REPLY)
                        .title(REPLY_TITLE)
                        .content(REPLY_CONTENT)
                        .build(),
                defaultAlarm()
                        .id("dm_alarm_" + userId)
                        .userId(userId)
                        .type(AlarmType.DIRECT_MESSAGE)
                        .title(DM_TITLE)
                        .content(DM_CONTENT)
                        .build()
        ));
    }

    public void createReadAlarm(long userId) {
        Alarm alarm = defaultAlarm()
                .id("read_alarm_" + userId)
                .userId(userId)
                .isRead(true)
                .build();
        alarmRepository.save(alarm);
    }

    public Alarm createUnReadAlarm(long userId) {
        Alarm alarm = defaultAlarm()
                .userId(userId)
                .build();
        return alarmRepository.save(alarm);
    }
}

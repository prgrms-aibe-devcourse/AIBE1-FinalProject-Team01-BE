package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class BoardAccessPolicyTest {

    @Test
    void COMMUNITY_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.COMMUNITY, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.COMMUNITY, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.COMMUNITY, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.COMMUNITY, "read")).isTrue();
    }

    @Test
    void COMMUNITY_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.COMMUNITY, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.COMMUNITY, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.COMMUNITY, "write")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.COMMUNITY, "write")).isTrue();
    }

    @Test
    void TOGETHER_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.TOGETHER, "read")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.TOGETHER, "read")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.TOGETHER, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.TOGETHER, "read")).isTrue();
    }

    @Test
    void TOGETHER_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.TOGETHER, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.TOGETHER, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.TOGETHER, "write")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.TOGETHER, "write")).isTrue();
    }

    @Test
    void IT_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.IT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.IT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.IT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.IT, "read")).isTrue();
    }

    @Test
    void IT_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.IT, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.IT, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.IT, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.IT, "write")).isTrue();
    }

    @Test
    void PROJECT_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.PROJECT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.PROJECT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.PROJECT, "read")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.PROJECT, "read")).isTrue();
    }

    @Test
    void PROJECT_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(BoardAccessPolicy.canAccess(Role.ANONYMOUS, BoardCategory.PROJECT, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.GUEST, BoardCategory.PROJECT, "write")).isFalse();
        assertThat(BoardAccessPolicy.canAccess(Role.STUDENT, BoardCategory.PROJECT, "write")).isTrue();
        assertThat(BoardAccessPolicy.canAccess(Role.ADMIN, BoardCategory.PROJECT, "write")).isTrue();
    }

    @Test
    void STUDENT_권한으로_COMMUNITY_게시판_접근을_검증한다() {
        // given
        Role userRole = Role.STUDENT;
        BoardType boardType = BoardType.FREE;
        String operation = "read";

        // when & then - 예외가 발생하지 않아야 함
        BoardAccessPolicy.validateAccess(userRole, boardType, operation);
    }

    @Test
    void ANONYMOUS_권한으로_TOGETHER_게시판_접근_시_예외가_발생한다() {
        // given
        Role userRole = Role.ANONYMOUS;
        BoardType boardType = BoardType.GATHER;
        String operation = "read";

        // when & then
        assertThatThrownBy(() -> BoardAccessPolicy.validateAccess(userRole, boardType, operation))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void GUEST_권한으로_IT_게시판_쓰기_시_예외가_발생한다() {
        // given
        Role userRole = Role.GUEST;
        BoardType boardType = BoardType.INFO;
        String operation = "write";

        // when & then
        assertThatThrownBy(() -> BoardAccessPolicy.validateAccess(userRole, boardType, operation))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void ADMIN_권한은_모든_게시판에_접근할_수_있다() {
        // given
        Role userRole = Role.ADMIN;
        String operation = "write";

        // when & then
        BoardAccessPolicy.validateAccess(userRole, BoardType.FREE, operation);
        BoardAccessPolicy.validateAccess(userRole, BoardType.QNA, operation);
        BoardAccessPolicy.validateAccess(userRole, BoardType.MARKET, operation);
        BoardAccessPolicy.validateAccess(userRole, BoardType.GATHER, operation);
        BoardAccessPolicy.validateAccess(userRole, BoardType.INFO, operation);
        BoardAccessPolicy.validateAccess(userRole, BoardType.REVIEW, operation);
    }

    @Test
    void 게시판_타입이_잘못된_카테고리에_속할_때_예외가_발생한다() {
        // given
        BoardType boardType = BoardType.FREE;
        BoardCategory wrongCategory = BoardCategory.TOGETHER;

        // when & then
        assertThatThrownBy(() -> BoardAccessPolicy.boardTypeInCategory(boardType, wrongCategory))
                .isInstanceOf(CustomException.class);
    }
}

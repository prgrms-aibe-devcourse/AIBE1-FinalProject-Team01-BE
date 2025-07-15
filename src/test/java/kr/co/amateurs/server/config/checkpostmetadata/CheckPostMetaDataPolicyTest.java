package kr.co.amateurs.server.config.checkpostmetadata;

import kr.co.amateurs.server.annotation.checkpostmetadata.CheckPostMetaDataPolicy;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class CheckPostMetaDataPolicyTest {

    private CheckPostMetaDataPolicy checkPostMetaDataPolicy;

    @BeforeEach
    public void setUp() {
        checkPostMetaDataPolicy = new CheckPostMetaDataPolicy();
    }

    @Test
    void COMMUNITY_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.COMMUNITY, OperationType.READ)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.COMMUNITY, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.COMMUNITY, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.COMMUNITY, OperationType.READ)).isTrue();
    }

    @Test
    void COMMUNITY_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.COMMUNITY, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.COMMUNITY, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.COMMUNITY, OperationType.WRITE)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.COMMUNITY, OperationType.WRITE)).isTrue();
    }

    @Test
    void TOGETHER_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.TOGETHER, OperationType.READ)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.TOGETHER, OperationType.READ)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.TOGETHER, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.TOGETHER, OperationType.READ)).isTrue();
    }

    @Test
    void TOGETHER_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.TOGETHER, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.TOGETHER, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.TOGETHER, OperationType.WRITE)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.TOGETHER, OperationType.WRITE)).isTrue();
    }

    @Test
    void IT_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.IT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.IT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.IT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.IT, OperationType.READ)).isTrue();
    }

    @Test
    void IT_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.IT, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.IT, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.IT, OperationType.WRITE)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.IT, OperationType.WRITE)).isTrue();
    }

    @Test
    void PROJECT_카테고리_읽기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.PROJECT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.PROJECT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.PROJECT, OperationType.READ)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.PROJECT, OperationType.READ)).isTrue();
    }

    @Test
    void PROJECT_카테고리_쓰기_권한을_확인한다() {
        // given & when & then
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ANONYMOUS, BoardCategory.PROJECT, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.GUEST, BoardCategory.PROJECT, OperationType.WRITE)).isFalse();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.STUDENT, BoardCategory.PROJECT, OperationType.WRITE)).isTrue();
        assertThat(checkPostMetaDataPolicy.canAccess(Role.ADMIN, BoardCategory.PROJECT, OperationType.WRITE)).isTrue();
    }

    @Test
    void STUDENT_권한으로_COMMUNITY_게시판_접근을_검증한다() {
        // given
        Role userRole = Role.STUDENT;
        BoardType boardType = BoardType.FREE;
        OperationType operationType = OperationType.READ;

        // when & then - 예외가 발생하지 않아야 함
        checkPostMetaDataPolicy.validateAccess(userRole, boardType, operationType);
    }

    @Test
    void ANONYMOUS_권한으로_TOGETHER_게시판_접근_시_예외가_발생한다() {
        // given
        Role userRole = Role.ANONYMOUS;
        BoardType boardType = BoardType.GATHER;
        OperationType operationType = OperationType.READ;

        // when & then
        assertThatThrownBy(() -> checkPostMetaDataPolicy.validateAccess(userRole, boardType, operationType))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void GUEST_권한으로_IT_게시판_쓰기_시_예외가_발생한다() {
        // given
        Role userRole = Role.GUEST;
        BoardType boardType = BoardType.REVIEW;
        OperationType operationType = OperationType.WRITE;

        // when & then
        assertThatThrownBy(() -> checkPostMetaDataPolicy.validateAccess(userRole, boardType, operationType))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void ADMIN_권한은_모든_게시판에_접근할_수_있다() {
        // given
        Role userRole = Role.ADMIN;
        OperationType operationType = OperationType.WRITE;

        // when & then
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.FREE, operationType);
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.QNA, operationType);
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.MARKET, operationType);
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.GATHER, operationType);
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.REVIEW, operationType);
        checkPostMetaDataPolicy.validateAccess(userRole, BoardType.REVIEW, operationType);
    }

    @Test
    void 게시판_타입이_잘못된_카테고리에_속할_때_예외가_발생한다() {
        // given
        BoardType boardType = BoardType.FREE;
        BoardCategory wrongCategory = BoardCategory.TOGETHER;

        // when & then
        assertThatThrownBy(() -> checkPostMetaDataPolicy.boardTypeInCategory(boardType, wrongCategory))
                .isInstanceOf(CustomException.class);
    }
}

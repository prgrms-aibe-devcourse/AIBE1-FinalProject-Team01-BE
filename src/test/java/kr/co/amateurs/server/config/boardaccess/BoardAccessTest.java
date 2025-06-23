package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BoardAccessTest {

    @Test
    void 어노테이션의_기본값을_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess
            public void testMethod() {}
        }

        // when
        BoardAccess annotation = TestClass.class.getMethod("testMethod").getAnnotation(BoardAccess.class);

        // then
        assertThat(annotation.category()).isEqualTo(BoardCategory.COMMUNITY);
        assertThat(annotation.needCategory()).isFalse();
        assertThat(annotation.boardType()).isEqualTo(BoardType.GATHER);
        assertThat(annotation.hasBoardType()).isTrue();
        assertThat(annotation.hasPostId()).isFalse();
        assertThat(annotation.operation()).isEqualTo("read");
    }

    @Test
    void 어노테이션의_사용자_입력_값을_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(
                    category = BoardCategory.IT,
                    needCategory = true,
                    boardType = BoardType.INFO,
                    hasBoardType = false,
                    hasPostId = true,
                    operation = "write"
            )
            public void customTestMethod() {}
        }

        // when
        BoardAccess annotation = TestClass.class.getMethod("customTestMethod").getAnnotation(BoardAccess.class);

        // then
        assertThat(annotation.category()).isEqualTo(BoardCategory.IT);
        assertThat(annotation.needCategory()).isTrue();
        assertThat(annotation.boardType()).isEqualTo(BoardType.INFO);
        assertThat(annotation.hasBoardType()).isFalse();
        assertThat(annotation.hasPostId()).isTrue();
        assertThat(annotation.operation()).isEqualTo("write");
    }

    @Test
    void 특정_카테고리_설정을_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(
                    category = BoardCategory.TOGETHER,
                    boardType = BoardType.MARKET,
                    operation = "write"
            )
            public void togetherMethod() {}
        }

        // when
        BoardAccess annotation = TestClass.class.getMethod("togetherMethod").getAnnotation(BoardAccess.class);

        // then
        assertThat(annotation.category()).isEqualTo(BoardCategory.TOGETHER);
        assertThat(annotation.boardType()).isEqualTo(BoardType.MARKET);
        assertThat(annotation.operation()).isEqualTo("write");
    }

    @Test
    void 모든_BoardType과_매핑되는_어노테이션을_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(boardType = BoardType.FREE)
            public void freeBoard() {}

            @BoardAccess(boardType = BoardType.QNA)
            public void qnaBoard() {}

            @BoardAccess(boardType = BoardType.MARKET)
            public void marketBoard() {}

            @BoardAccess(boardType = BoardType.RETROSPECT)
            public void retrospectBoard() {}

            @BoardAccess(boardType = BoardType.GATHER)
            public void gatherBoard() {}

            @BoardAccess(boardType = BoardType.MATCH)
            public void matchBoard() {}

            @BoardAccess(boardType = BoardType.PROJECT_HUB)
            public void projectHubBoard() {}

            @BoardAccess(boardType = BoardType.INFO)
            public void infoBoard() {}

            @BoardAccess(boardType = BoardType.REVIEW)
            public void reviewBoard() {}
        }

        // when & then
        assertThat(TestClass.class.getMethod("freeBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.FREE);
        assertThat(TestClass.class.getMethod("qnaBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.QNA);
        assertThat(TestClass.class.getMethod("marketBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.MARKET);
        assertThat(TestClass.class.getMethod("retrospectBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.RETROSPECT);
        assertThat(TestClass.class.getMethod("gatherBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.GATHER);
        assertThat(TestClass.class.getMethod("matchBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.MATCH);
        assertThat(TestClass.class.getMethod("projectHubBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.PROJECT_HUB);
        assertThat(TestClass.class.getMethod("infoBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.INFO);
        assertThat(TestClass.class.getMethod("reviewBoard").getAnnotation(BoardAccess.class).boardType())
                .isEqualTo(BoardType.REVIEW);
    }

    @Test
    void 읽기와_쓰기_작업을_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(operation = "read")
            public void readMethod() {}

            @BoardAccess(operation = "write")
            public void writeMethod() {}
        }

        // when & then
        assertThat(TestClass.class.getMethod("readMethod").getAnnotation(BoardAccess.class).operation())
                .isEqualTo("read");
        assertThat(TestClass.class.getMethod("writeMethod").getAnnotation(BoardAccess.class).operation())
                .isEqualTo("write");
    }

    @Test
    void postId_사용_여부를_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(hasPostId = true)
            public void withPostIdMethod() {}

            @BoardAccess(hasPostId = false)
            public void withoutPostIdMethod() {}
        }

        // when & then
        assertThat(TestClass.class.getMethod("withPostIdMethod").getAnnotation(BoardAccess.class).hasPostId())
                .isTrue();
        assertThat(TestClass.class.getMethod("withoutPostIdMethod").getAnnotation(BoardAccess.class).hasPostId())
                .isFalse();
    }

    @Test
    void boardType_사용_여부를_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(hasBoardType = true)
            public void withBoardTypeMethod() {}

            @BoardAccess(hasBoardType = false)
            public void withoutBoardTypeMethod() {}
        }

        // when & then
        assertThat(TestClass.class.getMethod("withBoardTypeMethod").getAnnotation(BoardAccess.class).hasBoardType())
                .isTrue();
        assertThat(TestClass.class.getMethod("withoutBoardTypeMethod").getAnnotation(BoardAccess.class).hasBoardType())
                .isFalse();
    }

    @Test
    void 카테고리_검증_필요_여부를_확인한다() throws NoSuchMethodException {
        // given
        class TestClass {
            @BoardAccess(needCategory = true)
            public void needCategoryMethod() {}

            @BoardAccess(needCategory = false)
            public void notNeedCategoryMethod() {}
        }

        // when & then
        assertThat(TestClass.class.getMethod("needCategoryMethod").getAnnotation(BoardAccess.class).needCategory())
                .isTrue();
        assertThat(TestClass.class.getMethod("notNeedCategoryMethod").getAnnotation(BoardAccess.class).needCategory())
                .isFalse();
    }
}

package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class PostRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    void 특정_보드타입으로_조회하면_해당_게시글들이_반환되어야_한다() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Post post = Post.builder()
                .user(user)
                .title("테스트 제목")
                .content("테스트 내용")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(post);

        // When
        Page<Post> result = postRepository.findByBoardType(
                BoardType.FREE,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    void 제목에_키워드가_포함된_게시글을_보드타입으로_조회하면_해당_게시글이_반환되어야_한다() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Post post = Post.builder()
                .user(user)
                .title("자바 프로그래밍 질문")
                .content("내용입니다")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(post);

        // When
        Page<Post> result = postRepository.findByContentAndBoardType(
                "자바",
                BoardType.FREE,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("자바 프로그래밍 질문");
    }

    @Test
    void 내용에_키워드가_포함된_게시글을_보드타입으로_조회하면_해당_게시글이_반환되어야_한다() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Post post = Post.builder()
                .user(user)
                .title("질문 제목")
                .content("스프링 부트 설정 방법을 알고 싶습니다")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(post);

        // When
        Page<Post> result = postRepository.findByContentAndBoardType(
                "스프링",
                BoardType.FREE,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).contains("스프링 부트");
    }

    @Test
    void 키워드가_없는_게시글을_조회하면_빈_결과가_반환되어야_한다() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Post post = Post.builder()
                .user(user)
                .title("일반 제목")
                .content("일반 내용")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(post);

        // When
        Page<Post> result = postRepository.findByContentAndBoardType(
                "존재하지않는키워드",
                BoardType.FREE,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 다른_보드타입의_게시글은_조회되지_않아야_한다() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("테스트유저")
                .role(Role.STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Post freePost = Post.builder()
                .user(user)
                .title("자유게시판 글")
                .content("자유게시판 내용")
                .boardType(BoardType.FREE)
                .build();
        entityManager.persistAndFlush(freePost);

        Post qnaPost = Post.builder()
                .user(user)
                .title("질문/토론 글")
                .content("질문/토론 내용")
                .boardType(BoardType.QNA)
                .build();
        entityManager.persistAndFlush(qnaPost);

        // When
        Page<Post> result = postRepository.findByContentAndBoardType(
                "글",
                BoardType.FREE,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBoardType()).isEqualTo(BoardType.FREE);
    }
}
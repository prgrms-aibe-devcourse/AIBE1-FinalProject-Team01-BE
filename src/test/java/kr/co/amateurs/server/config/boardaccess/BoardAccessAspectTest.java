package kr.co.amateurs.server.config.boardaccess;

import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BoardAccessAspectTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Method method;

    @Mock
    private Parameter parameter;

    @Mock
    private PathVariable pathVariable;

    @Mock
    private BoardAccess boardAccess;

    @InjectMocks
    private BoardAccessAspect boardAccessAspect;

    private User adminUser;
    private User studentUser;
    private User guestUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        studentUser = User.builder()
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        guestUser = User.builder()
                .email("guest@test.com")
                .role(Role.GUEST)
                .build();

        testPost = Post.builder()
                .boardType(BoardType.FREE)
                .build();
    }

    @Test
    void ADMIN_권한은_모든_접근을_허용한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(adminUser));

        // when & then
        assertDoesNotThrow(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess));

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void postId로_게시판_타입을_조회하여_접근을_검증한다() {
        // given
        Long postId = 1L;
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(true);
        when(boardAccess.operation()).thenReturn("read");
        when(boardAccess.needCategory()).thenReturn(false);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
        when(joinPoint.getArgs()).thenReturn(new Object[]{postId});
        when(parameter.getAnnotation(PathVariable.class)).thenReturn(pathVariable);
        when(pathVariable.value()).thenReturn("postId");
        when(parameter.getType()).thenReturn((Class) Long.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

        // when & then
        assertDoesNotThrow(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess));

        // verify
        verify(userService).getCurrentUser();
        verify(postRepository).findById(postId);
    }

    @Test
    void 존재하지_않는_postId로_접근_시_예외가_발생한다() {
        // given
        Long nonExistentPostId = 999L;
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
        when(joinPoint.getArgs()).thenReturn(new Object[]{nonExistentPostId});
        when(parameter.getAnnotation(PathVariable.class)).thenReturn(pathVariable);
        when(pathVariable.value()).thenReturn("postId");
        when(parameter.getType()).thenReturn((Class) Long.class);

        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess))
                .isInstanceOf(CustomException.class);

        // verify
        verify(userService).getCurrentUser();
        verify(postRepository).findById(nonExistentPostId);
    }

    @Test
    void boardType_파라미터로_접근을_검증한다() {
        // given
        BoardType boardType = BoardType.FREE;
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(true);
        when(boardAccess.operation()).thenReturn("read");
        when(boardAccess.needCategory()).thenReturn(false);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
        when(joinPoint.getArgs()).thenReturn(new Object[]{boardType});
        when(parameter.getAnnotation(PathVariable.class)).thenReturn(pathVariable);
        when(pathVariable.value()).thenReturn("boardType");
        when(parameter.getType()).thenReturn((Class) BoardType.class);

        // when & then
        assertDoesNotThrow(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess));

        // verify
        verify(userService).getCurrentUser();
    }

    @Test
    void 어노테이션의_기본_boardType을_사용하여_접근을_검증한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(false);
        when(boardAccess.boardType()).thenReturn(BoardType.FREE);
        when(boardAccess.operation()).thenReturn("read");
        when(boardAccess.needCategory()).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess));

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void 카테고리_검증이_필요한_경우_카테고리를_확인한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(false);
        when(boardAccess.boardType()).thenReturn(BoardType.FREE);
        when(boardAccess.operation()).thenReturn("read");
        when(boardAccess.needCategory()).thenReturn(true);
        when(boardAccess.category()).thenReturn(BoardCategory.COMMUNITY);

        // when & then
        assertDoesNotThrow(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess));

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void 잘못된_카테고리_검증_시_예외가_발생한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(false);
        when(boardAccess.boardType()).thenReturn(BoardType.FREE);
        when(boardAccess.needCategory()).thenReturn(true);
        when(boardAccess.category()).thenReturn(BoardCategory.TOGETHER);

        // when & then
        assertThatThrownBy(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess))
                .isInstanceOf(CustomException.class);

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void GUEST_권한으로_IT_게시판_쓰기_접근_시_예외가_발생한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(guestUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(false);
        when(boardAccess.boardType()).thenReturn(BoardType.INFO);
        when(boardAccess.operation()).thenReturn("write");
        when(boardAccess.needCategory()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess))
                .isInstanceOf(CustomException.class);

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void 현재_사용자가_없는_경우_ANONYMOUS로_처리한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.empty());
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(false);
        when(boardAccess.boardType()).thenReturn(BoardType.REVIEW);
        when(boardAccess.operation()).thenReturn("read");
        when(boardAccess.needCategory()).thenReturn(false);

        // when & then
        boardAccessAspect.checkBoardAccess(joinPoint, boardAccess);

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void postId_파라미터를_찾을_수_없는_경우_예외가_발생한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"wrongParam"});
        when(parameter.getAnnotation(PathVariable.class)).thenReturn(pathVariable);
        when(pathVariable.value()).thenReturn("wrongParam");

        // when & then
        assertThatThrownBy(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess))
                .isInstanceOf(CustomException.class);

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }

    @Test
    void boardType_파라미터를_찾을_수_없는_경우_예외가_발생한다() {
        // given
        when(userService.getCurrentUser()).thenReturn(Optional.of(studentUser));
        when(boardAccess.hasPostId()).thenReturn(false);
        when(boardAccess.hasBoardType()).thenReturn(true);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"wrongParam"});
        when(parameter.getAnnotation(PathVariable.class)).thenReturn(pathVariable);
        when(pathVariable.value()).thenReturn("wrongParam");

        // when & then
        assertThatThrownBy(() -> boardAccessAspect.checkBoardAccess(joinPoint, boardAccess))
                .isInstanceOf(CustomException.class);

        // verify
        verify(userService).getCurrentUser();
        verifyNoInteractions(postRepository);
    }
}

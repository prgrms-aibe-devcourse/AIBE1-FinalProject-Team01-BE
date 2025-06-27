package kr.co.amateurs.server.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.domain.dto.comment.CommentPageDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentRequestDTO;
import kr.co.amateurs.server.domain.dto.comment.CommentResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.service.comment.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 특정_게시글이_존재할때_유저가_해당_페이지에_접근하면_댓글이_페이지_사이즈만큼_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        int size = 10;
        List<CommentResponseDTO> comments = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            comments.add(new CommentResponseDTO((long) i, postId,"testUser",null, DevCourseTrack.AI_BACKEND, null, "댓글 내용 " + i, 0, 0, false, LocalDateTime.now(), LocalDateTime.now()));
        }

        CommentPageDTO commentPageDTO = new CommentPageDTO(comments, null, false);

        given(commentService.getCommentsByPostId(eq(postId), isNull(), eq(size)))
                .willReturn(commentPageDTO);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(size))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 댓글이_없는_게시글에_접근하면_빈_댓글_목록이_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        CommentPageDTO emptyCommentPage = new CommentPageDTO(new ArrayList<>(), null, false);

        given(commentService.getCommentsByPostId(eq(postId), isNull(), eq(10)))
                .willReturn(emptyCommentPage);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 커서_기반_페이징으로_댓글을_요청하면_다음_페이지_댓글이_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        Long cursor = 10L;
        int size = 5;

        List<CommentResponseDTO> comments = new ArrayList<>();
        for (int i = 11; i <= 15; i++) {
            comments.add(new CommentResponseDTO((long) i, postId,"testUser",null, DevCourseTrack.AI_BACKEND,null, "댓글 내용 " + i, 0, 0, false, LocalDateTime.now(), LocalDateTime.now()));
        }

        CommentPageDTO commentPageDTO = new CommentPageDTO(comments, 15L, true);

        given(commentService.getCommentsByPostId(eq(postId), eq(cursor), eq(size)))
                .willReturn(commentPageDTO);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
                        .param("cursor", String.valueOf(cursor))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(size))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(15));
    }

    @Test
    void 특정_댓글의_답글을_요청하면_답글_목록이_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        Long commentId = 5L;
        int size = 5;

        List<CommentResponseDTO> replies = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            replies.add(new CommentResponseDTO((long) i, postId,"testUser",null, DevCourseTrack.AI_BACKEND,commentId, "답글 내용 " + i, 0, 0, false, LocalDateTime.now(), LocalDateTime.now()));
        }

        CommentPageDTO replyPageDTO = new CommentPageDTO(replies, null, false);

        given(commentService.getReplies(eq(postId), eq(commentId), isNull(), eq(size)))
                .willReturn(replyPageDTO);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(size))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 대댓글이_없는_댓글에_요청하면_빈_대댓글_목록이_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        Long commentId = 5L;

        CommentPageDTO emptyReplyPage = new CommentPageDTO(new ArrayList<>(), null, false);

        given(commentService.getReplies(eq(postId), eq(commentId), isNull(), eq(5)))
                .willReturn(emptyReplyPage);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 유효한_댓글_데이터로_생성_요청하면_댓글이_생성되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(100L,"새로운 댓글 내용");

        CommentResponseDTO responseDTO = new CommentResponseDTO(1L, postId,"testUser",null, DevCourseTrack.AI_BACKEND,100L, "새로운 댓글 내용", 0, 0, false, LocalDateTime.now(), LocalDateTime.now());

        given(commentService.createComment(eq(postId), any(CommentRequestDTO.class)))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentCommentId").value(100))
                .andExpect(jsonPath("$.content").value("새로운 댓글 내용"))
                .andExpect(jsonPath("$.nickname").value("testUser"));
    }

    @Test
    void 유효한_수정_데이터로_댓글_수정_요청하면_댓글이_수정되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        Long commentId = 5L;
        CommentRequestDTO requestDTO = new CommentRequestDTO(100L,"수정된 댓글 내용");

        willDoNothing().given(commentService)
                .updateComment(eq(postId), eq(commentId), any(CommentRequestDTO.class));

        // when & then
        mockMvc.perform(put("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하는_댓글_삭제_요청하면_댓글이_삭제되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        Long commentId = 5L;

        willDoNothing().given(commentService)
                .deleteComment(eq(postId), eq(commentId));

        // when & then
        mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isNoContent());
    }


}

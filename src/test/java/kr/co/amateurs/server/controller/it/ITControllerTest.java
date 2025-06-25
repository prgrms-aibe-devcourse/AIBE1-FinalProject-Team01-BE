package kr.co.amateurs.server.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.domain.dto.it.ITRequestDTO;
import kr.co.amateurs.server.domain.dto.it.ITResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.service.it.ITService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ITController.class)
@Import({TestSecurityConfig.class})
public class ITControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITService itService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 유저가_키워드없이_검색하면_전체_게시글_목록이_반환되어야_한다() throws Exception {
        // given
        Page<ITResponseDTO> mockPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 8),
                0
        );

        given(itService.searchPosts(any(), eq(0), eq(BoardType.REVIEW), eq(SortType.LATEST), eq(8)))
                .willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}", BoardType.REVIEW)
                        .param("page", "0")
                        .param("sortType", "LATEST")
                        .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(8))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void 유저가_키워드로_검색하면_해당_게시글_목록이_반환되어야_한다() throws Exception {
        // given
        String keyword = "테스트";
        Page<ITResponseDTO> mockPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 8),
                0
        );

        given(itService.searchPosts(eq(keyword), eq(0), eq(BoardType.REVIEW), eq(SortType.LATEST), eq(8)))
                .willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}", BoardType.REVIEW)
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("sortType", "LATEST")
                        .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(8))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void 유저가_특정_게시글을_조회하면_게시글_상세정보가_반환되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        ITResponseDTO mockResponseDTO = new ITResponseDTO(
                postId,
                "테스트 제목",
                "테스트 내용",
                "테스트 작성자",
                "http://example.com/profile.jpg",
                "AIBE",
                "1기",
                BoardType.REVIEW,
                0,
                0,
                0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                false,
                false,
                false
        );

        given(itService.getPost(eq(postId)))
                .willReturn(mockResponseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}/{postId}", BoardType.REVIEW, postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.nickname").value("테스트 작성자"))
                .andExpect(jsonPath("$.boardType").value("REVIEW"));
    }

    @Test
    void 유저가_게시글을_작성하면_새로운_게시글이_생성되어야_한다() throws Exception {
        // given
        ITRequestDTO requestDTO = new ITRequestDTO(
                "새 게시글 제목",
                "태그",
                "새 게시글 내용"
        );

        ITResponseDTO responseDTO = new ITResponseDTO(
                1L,
                "새 게시글 제목",
                "새 게시글 내용",
                "테스트 작성자",
                "http://example.com/profile.jpg",
                "AIBE",
                "1기",
                BoardType.REVIEW,
                0,
                0,
                0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "태그",
                false,
                false,
                false
        );

        given(itService.createPost(any(ITRequestDTO.class), eq(BoardType.REVIEW)))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/IT/{boardType}", BoardType.REVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.title").value("새 게시글 제목"))
                .andExpect(jsonPath("$.content").value("새 게시글 내용"));
    }

    @Test
    void 유저가_게시글을_수정하면_게시글_내용이_변경되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        ITRequestDTO requestDTO = new ITRequestDTO(
                "수정된 게시글 제목",
                "수정된 태그",
                "수정된 게시글 내용"
        );

        doNothing().when(itService).updatePost(any(ITRequestDTO.class), eq(postId));

        // when & then
        mockMvc.perform(put("/api/v1/IT/{boardType}/{postId}", BoardType.REVIEW, postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void 유저가_게시글을_삭제하면_게시글이_제거되어야_한다() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(itService).deletePost(eq(postId));

        // when & then
        mockMvc.perform(delete("/api/v1/IT/{boardType}/{postId}", BoardType.REVIEW, postId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 유저가_잘못된_보드타입으로_요청하면_400에러가_발생해야_한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}", "INVALID_BOARD_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유저가_잘못된_정렬타입으로_요청하면_400에러가_발생해야_한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}", BoardType.REVIEW)
                        .param("sortType", "INVALID_SORT_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유저가_음수_페이지로_요청하면_400에러가_발생해야_한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/IT/{boardType}", BoardType.REVIEW)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
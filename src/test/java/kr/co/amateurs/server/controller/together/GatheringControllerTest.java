package kr.co.amateurs.server.controller.together;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.SecurityConfig;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.service.together.GatheringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GatheringController.class)
@Import(TestSecurityConfig.class)
public class GatheringControllerTest {

    @MockitoBean
    private GatheringService gatheringService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        User user = User.builder().build();
        CustomUserDetails currentUser = new CustomUserDetails(user);
    }

    @Test
    void 검색어없이_기본파라미터로_목록조회하면_200OK와_전체목록반환() throws Exception {
        // given
        List<GatheringPostResponseDTO> gatheringPosts = Arrays.asList(
                createGatheringPostResponseDTO(1L, "첫 번째 모집", "첫 번째 내용"),
                createGatheringPostResponseDTO(2L, "두 번째 모집", "두 번째 내용")
        );
        Page<GatheringPostResponseDTO> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);
        given(gatheringService.getGatheringPostList(null, paginationParam)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/gatherings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 검색어를_포함하여_목록조회하면_200OK와_필터링된목록반환() throws Exception {
        // given
        String keyword = "스터디";
        List<GatheringPostResponseDTO> searchResults = Arrays.asList(
                createGatheringPostResponseDTO(1L, "Java 스터디", "Java 스터디 모집합니다")
        );
        Page<GatheringPostResponseDTO> page = new PageImpl<>(searchResults);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.LATEST);

        given(gatheringService.getGatheringPostList(keyword, paginationParam)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 페이지와_사이즈를_지정하여_목록조회하면_200OK와_해당페이지반환() throws Exception {
        // given
        List<GatheringPostResponseDTO> gatheringPosts = Arrays.asList(
                createGatheringPostResponseDTO(1L, "첫 번째 모집", "첫 번째 내용")
        );
        Page<GatheringPostResponseDTO> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(1, 5, Sort.Direction.DESC, PaginationSortType.LATEST);
        given(gatheringService.getGatheringPostList(null, paginationParam)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 정렬조건을_지정하여_목록조회하면_200OK와_정렬된목록반환() throws Exception {
        // given
        List<GatheringPostResponseDTO> gatheringPosts = Arrays.asList(
                createGatheringPostResponseDTO(1L, "인기 모집", "인기 내용")
        );
        Page<GatheringPostResponseDTO> page = new PageImpl<>(gatheringPosts);
        PaginationParam paginationParam = new PaginationParam(0, 10, Sort.Direction.DESC, PaginationSortType.POPULAR);

        given(gatheringService.getGatheringPostList(null, paginationParam)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("sortType", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 존재하는_게시글ID로_단건조회하면_200OK와_게시글정보반환() throws Exception {
        // given
        Long gatheringId = 1L;
        GatheringPostResponseDTO responseDTO = createGatheringPostResponseDTO(
                gatheringId, "테스트 모집", "테스트 내용"
        );

        given(gatheringService.getGatheringPost(gatheringId))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/gatherings/{gatheringId}", gatheringId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(gatheringId));
    }

    @Test
    void 유효한_데이터로_게시글생성하면_201CREATED와_생성된게시글반환() throws Exception {
        // given
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();
        GatheringPostResponseDTO responseDTO = createGatheringPostResponseDTO(
                1L, requestDTO.title(), requestDTO.content()
        );

        given(gatheringService.createGatheringPost(currentUser, any(GatheringPostRequestDTO.class)))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/gatherings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(requestDTO.title()))
                .andExpect(jsonPath("$.content").value(requestDTO.content()));
    }

    @Test
    void 유효한_데이터로_게시글수정하면_204NOCONTENT반환() throws Exception {
        // given
        Long gatheringId = 1L;
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();

        doNothing().when(gatheringService).updateGatheringPost(currentUser, eq(gatheringId), any(GatheringPostRequestDTO.class));

        // when & then
        mockMvc.perform(put("/api/v1/gatherings/{gatheringId}", gatheringId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하는_게시글삭제하면_204NOCONTENT반환() throws Exception {
        // given
        Long gatheringId = 1L;

        doNothing().when(gatheringService).deleteGatheringPost(currentUser, gatheringId);

        // when & then
        mockMvc.perform(delete("/api/v1/gatherings/{gatheringId}", gatheringId))
                .andExpect(status().isNoContent());
    }

    
    //TODO - 밸리데이션 적용 시 아래 테스트들 정상 작동 예정 현재는 비정상 테스트
    /*
    @Test
    void 잘못된_정렬타입으로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("sortType", "INVALID_SORT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 음수페이지로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 양수가아닌사이즈로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 잘못된_파라미터타입으로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/gatherings")
                        .param("page", "invalid")
                        .param("size", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 숫자가아닌_pathVariable로_단건조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/gatherings/{gatheringId}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 잘못된_enum값으로_게시글생성하면_400에러반환() throws Exception {
        // given
        String invalidJson = """
            {
                "userId": 1,
                "title": "테스트",
                "content": "내용",
                "tags": "태그",
                "gatheringType": "INVALID_TYPE",
                "status": "RECRUITING",
                "headCount": 5,
                "place": "서울",
                "period": "1개월",
                "schedule": "주 2회"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/gatherings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지않는_게시글ID로_단건조회하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        given(gatheringService.getGatheringPost(nonExistentId))
                .willThrow(new IllegalArgumentException("Post not found: " + nonExistentId));

        // when & then
        mockMvc.perform(get("/api/v1/gatherings/{gatheringId}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void 존재하지않는_게시글수정하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        GatheringPostRequestDTO requestDTO = createGatheringPostRequestDTO();

        doThrow(new IllegalArgumentException("Gathering Post not found: " + nonExistentId))
                .when(gatheringService).updateGatheringPost(eq(nonExistentId), any(GatheringPostRequestDTO.class));

        // when & then
        mockMvc.perform(put("/api/v1/gatherings/{gatheringId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void 존재하지않는_게시글삭제하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        doThrow(new RuntimeException("Post not found"))
                .when(gatheringService).deleteGatheringPost(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/v1/gatherings/{gatheringId}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }
    */


    private GatheringPostResponseDTO createGatheringPostResponseDTO(Long postId, String title, String content) {
        return GatheringPostResponseDTO.builder()
                .postId(postId)
                .userId(1L)
                .title(title)
                .content(content)
                .tags("태그")
                .viewCount(0)
                .likeCount(0)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(5)
                .place("서울")
                .period("1개월")
                .schedule("주 2회")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private GatheringPostRequestDTO createGatheringPostRequestDTO() {
        return new GatheringPostRequestDTO(
                1L, "테스트 모집", "테스트 내용", "태그",
                GatheringType.STUDY, GatheringStatus.RECRUITING,
                5, "서울", "1개월", "주 2회"
        );
    }

}

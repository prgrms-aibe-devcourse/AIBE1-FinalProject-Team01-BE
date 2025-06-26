package kr.co.amateurs.server.controller.together;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.TestSecurityConfig;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.service.together.MarketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MarketController.class)
@Import(TestSecurityConfig.class)
public class MarketControllerTest {

    @MockitoBean
    private MarketService marketService;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private CustomUserDetails currentUser;

    @BeforeEach
    public void setup() {
        User user = User.builder()
                .email("test@email.com")
                .password("password")
                .nickname("testUser")
                .name("이름")
                .role(Role.STUDENT)
                .build();
        currentUser = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                currentUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void 검색어없이_기본파라미터로_목록조회하면_200OK와_전체목록반환() throws Exception {
        // given
        List<MarketPostResponseDTO> marketPosts = Arrays.asList(
                createMarketPostResponseDTO(1L, "첫 번째 물건", "첫 번째 내용"),
                createMarketPostResponseDTO(2L, "두 번째 물건", "두 번째 내용")
        );
        Page<MarketPostResponseDTO> page = new PageImpl<>(marketPosts);

        given(marketService.getMarketPostList(any(PostPaginationParam.class))).willReturn(convertPageToDTO(page));

        // when & then
        mockMvc.perform(get("/api/v1/market"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 검색어를_포함하여_목록조회하면_200OK와_필터링된목록반환() throws Exception {
        // given
        String keyword = "책";
        List<MarketPostResponseDTO> searchResults = Arrays.asList(
                createMarketPostResponseDTO(1L, "Java 책", "Java 책 팝니다")
        );
        Page<MarketPostResponseDTO> page = new PageImpl<>(searchResults);

        given(marketService.getMarketPostList(any(PostPaginationParam.class))).willReturn(convertPageToDTO(page));

        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .with(user(currentUser))
                        .accept(MediaType.APPLICATION_JSON)
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 페이지와_사이즈를_지정하여_목록조회하면_200OK와_해당페이지반환() throws Exception {
        // given
        List<MarketPostResponseDTO> marketPosts = Arrays.asList(
                createMarketPostResponseDTO(1L, "첫 번째 물건", "첫 번째 내용")
        );
        Page<MarketPostResponseDTO> page = new PageImpl<>(marketPosts);

        given(marketService.getMarketPostList(any(PostPaginationParam.class))).willReturn(convertPageToDTO(page));

        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .with(user(currentUser))
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 정렬조건을_지정하여_목록조회하면_200OK와_정렬된목록반환() throws Exception {
        // given
        List<MarketPostResponseDTO> marketPosts = Arrays.asList(
                createMarketPostResponseDTO(1L, "인기 물건", "인기 내용")
        );
        Page<MarketPostResponseDTO> page = new PageImpl<>(marketPosts);

        given(marketService.getMarketPostList(any(PostPaginationParam.class)))
                .willReturn(convertPageToDTO(page));

        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .with(user(currentUser))
                        .accept(MediaType.APPLICATION_JSON)
                        .param("field", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 존재하는_게시글ID로_단건조회하면_200OK와_게시글정보반환() throws Exception {
        // given
        Long marketId = 1L;
        MarketPostResponseDTO responseDTO = createMarketPostResponseDTO(
                marketId, "테스트 물건", "테스트 내용"
        );

        given(marketService.getMarketPost(marketId))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/market/{marketId}", marketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(marketId));
    }

    @Test
    void 유효한_데이터로_게시글생성하면_201CREATED와_생성된게시글반환() throws Exception {
        // given
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();
        MarketPostResponseDTO responseDTO = createMarketPostResponseDTO(
                1L, requestDTO.title(), requestDTO.content()
        );

        given(marketService.createMarketPost(any(MarketPostRequestDTO.class)))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/market")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(requestDTO.title()))
                .andExpect(jsonPath("$.content").value(requestDTO.content()));
    }

    @Test
    void 유효한_데이터로_게시글수정하면_204NOCONTENT반환() throws Exception {
        // given
        Long marketId = 1L;
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();

        doNothing().when(marketService).updateMarketPost(eq(marketId), any(MarketPostRequestDTO.class));

        // when & then
        mockMvc.perform(put("/api/v1/market/{marketId}", marketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하는_게시글삭제하면_204NOCONTENT반환() throws Exception {
        // given
        Long marketId = 1L;
        doNothing().when(marketService).deleteMarketPost(marketId);

        // when & then
        mockMvc.perform(delete("/api/v1/market/{marketId}", marketId))
                .andExpect(status().isNoContent());
    }


    //TODO - 밸리데이션 적용 시 아래 테스트들 정상 작동 예정 현재는 비정상 테스트

    @Test
    void 잘못된_정렬타입으로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .param("field", "INVALID_SORT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 음수페이지로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 양수가아닌사이즈로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 잘못된_파라미터타입으로_목록조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/market")
                        .param("page", "invalid")
                        .param("size", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 숫자가아닌_pathVariable로_단건조회하면_400에러반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/market/{marketId}", "invalid"))
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
                "status": "INVALID_STATUS",
                "price": 1000,
                "place": "서울"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/market")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지않는_게시글ID로_단건조회하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        given(marketService.getMarketPost(nonExistentId))
                .willThrow(new IllegalArgumentException("Post not found: " + nonExistentId));

        // when & then
        mockMvc.perform(get("/api/v1/market/{marketId}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void 존재하지않는_게시글수정하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        MarketPostRequestDTO requestDTO = createMarketPostRequestDTO();

        doThrow(new IllegalArgumentException("Market Post not found: " + nonExistentId))
                .when(marketService).updateMarketPost(eq(nonExistentId), any(MarketPostRequestDTO.class));

        // when & then
        mockMvc.perform(put("/api/v1/market/{marketId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void 존재하지않는_게시글삭제하면_예외발생() throws Exception {
        // given
        Long nonExistentId = 999L;
        doThrow(new RuntimeException("Post not found"))
                .when(marketService).deleteMarketPost(nonExistentId);

        // when & then
        mockMvc.perform(delete("/api/v1/market/{marketId}", nonExistentId))
                .andExpect(status().isInternalServerError());
    }




    private MarketPostResponseDTO createMarketPostResponseDTO(Long postId, String title, String content) {
        return MarketPostResponseDTO.builder()
                .postId(postId)
                .nickname("nickname")
                .devcourseName("AIBE")
                .devcourseBatch("1기")
                .userProfileImg(null)
                .title(title)
                .content(content)
                .tags("태그")
                .viewCount(0)
                .likeCount(0)
                .status(MarketStatus.SELLING)
                .price(1000)
                .place("서울")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasLiked(false).hasBookmarked(false).hasImages(false)
                .build();
    }

    private MarketPostRequestDTO createMarketPostRequestDTO() {
        return new MarketPostRequestDTO(
                "테스트 물건", "테스트 내용", "태그", MarketStatus.SELLING, 1000, "서울"
        );
    }

}

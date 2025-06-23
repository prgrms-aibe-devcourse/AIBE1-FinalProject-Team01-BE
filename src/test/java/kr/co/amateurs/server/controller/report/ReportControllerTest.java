package kr.co.amateurs.server.controller.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.config.SecurityConfig;
import kr.co.amateurs.server.domain.dto.report.ReportRequestDTO;
import kr.co.amateurs.server.domain.dto.report.ReportResponseDTO;
import kr.co.amateurs.server.domain.entity.report.enums.ReportStatus;
import kr.co.amateurs.server.domain.entity.report.enums.ReportType;
import kr.co.amateurs.server.service.report.ReportService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
public class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 전체_신고_목록을_조회하면_모든_신고가_반환되어야_한다() throws Exception {
        // given
        List<ReportResponseDTO> mockReports = List.of(
                new ReportResponseDTO(null, null, "사용자1", "부적절한 내용", ReportStatus.PENDING),
                new ReportResponseDTO(null, null, "사용자2", "스팸 댓글", ReportStatus.REVIEWED)
        );
        Page<ReportResponseDTO> mockPage = new PageImpl<>(mockReports, PageRequest.of(0, 10), mockReports.size());

        given(reportService.getReports(any(ReportType.class), any(ReportStatus.class), eq(0), eq(10)))
                .willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/reports")
                        .param("reportType", "POST")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("사용자1"))
                .andExpect(jsonPath("$.content[0].description").value("부적절한 내용"))
                .andExpect(jsonPath("$.content[1].username").value("사용자2"))
                .andExpect(jsonPath("$.content[1].description").value("스팸 댓글"));
    }

    @Test
    void 유효한_신고_데이터로_생성_요청하면_신고가_생성되어야_한다() throws Exception {
        // given
        ReportRequestDTO requestDTO = new ReportRequestDTO(
                1L,
                ReportType.POST,
                "부적절한 게시글 내용입니다"
        );

        ReportResponseDTO responseDTO = new ReportResponseDTO(
                null,
                null,
                "신고자",
                "부적절한 게시글 내용입니다",
                ReportStatus.PENDING
        );

        given(reportService.createReport(any(ReportRequestDTO.class)))
                .willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("신고자"))
                .andExpect(jsonPath("$.description").value("부적절한 게시글 내용입니다"))
                .andExpect(jsonPath("$.reportStatus").value("PENDING"));
    }

    @Test
    void 신고_상태_업데이트_요청하면_상태가_변경되어야_한다() throws Exception {
        // given
        Long reportId = 1L;
        ReportStatus status = ReportStatus.REVIEWED;

        willDoNothing().given(reportService)
                .updateStatusReport(eq(reportId), eq(status));

        // when & then
        mockMvc.perform(put("/api/v1/reports/{reportId}/{status}", reportId, status))
                .andExpect(status().isNoContent());

        verify(reportService).updateStatusReport(reportId, status);
    }

    @Test
    void 신고_삭제_요청하면_신고가_삭제되어야_한다() throws Exception {
        // given
        Long reportId = 1L;

        willDoNothing().given(reportService)
                .deleteReport(eq(reportId));

        // when & then
        mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 잘못된_JSON_형식으로_신고_생성_요청하면_400에러가_발생해야_한다() throws Exception {
        // given
        String invalidJson = "{ invalid json }";

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 빈_내용으로_신고_생성_요청하면_400에러가_발생해야_한다() throws Exception {
        // given
        ReportRequestDTO requestDTO = new ReportRequestDTO(
                1L,
                ReportType.POST,
                ""
        );

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }


}

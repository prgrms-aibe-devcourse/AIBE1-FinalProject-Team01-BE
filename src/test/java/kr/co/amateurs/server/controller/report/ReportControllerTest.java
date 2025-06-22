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

        given(reportService.getAllReports()).willReturn(mockReports);

        // when & then
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("사용자1"))
                .andExpect(jsonPath("$[0].description").value("부적절한 내용"))
                .andExpect(jsonPath("$[1].username").value("사용자2"))
                .andExpect(jsonPath("$[1].description").value("스팸 댓글"));
    }

    @Test
    void 특정_상태의_신고를_조회하면_해당_상태의_신고만_반환되어야_한다() throws Exception {
        // given
        ReportStatus status = ReportStatus.PENDING;
        List<ReportResponseDTO> mockReports = List.of(
                new ReportResponseDTO(null, null, "사용자1", "대기 중인 신고1", ReportStatus.PENDING),
                new ReportResponseDTO(null, null, "사용자2", "대기 중인 신고2", ReportStatus.PENDING)
        );

        given(reportService.getReportsByStatus(eq(status))).willReturn(mockReports);

        // when & then
        mockMvc.perform(get("/api/v1/reports/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ReportType").value("PENDING"))
                .andExpect(jsonPath("$[1].ReportType").value("PENDING"));
    }

    @Test
    void 존재하지_않는_상태의_신고를_조회하면_빈_배열이_반환되어야_한다() throws Exception {
        // given
        ReportStatus status = ReportStatus.RESOLVED;
        given(reportService.getReportsByStatus(eq(status))).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/reports/{status}", status))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
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
                .andExpect(jsonPath("$.ReportType").value("PENDING"));
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
    void 잘못된_신고_상태로_요청하면_400에러가_발생해야_한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/reports/{status}", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
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

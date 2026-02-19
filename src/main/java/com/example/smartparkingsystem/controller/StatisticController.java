package com.example.smartparkingsystem.controller;

import com.example.smartparkingsystem.service.StatisticService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;


@Log4j2
@WebServlet(name = "statisticController", value= "/statistic/*")
public class StatisticController extends HttpServlet {


    private final StatisticService statisticService = StatisticService.INSTANCE;
    private final ObjectMapper objectMapper;

    public StatisticController() {
        this.objectMapper = new ObjectMapper();
        // LocalDateTime 직렬화를 위한 모듈 등록
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("doGet");

        String pathInfo = request.getPathInfo();

        // API 요청인 경우
        if (pathInfo != null && pathInfo.startsWith("/api/")) {
            handleApiRequest(request, response, pathInfo);
            return;
        }

        // 일반 페이지 요청만 JSP로 포워딩
        handlePageRequest(request, response);
    }

    private void handlePageRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("=== 통계 페이지 요청 ===");

        try {
            Map<String, Object> todaySummary = statisticService.getTodaySummary();
            request.setAttribute("todaySummary", todaySummary);
            log.info("오늘 요약 데이터 설정 완료: " + todaySummary);

            request.getRequestDispatcher("/web/statistic/statistic.jsp").forward(request, response);

        } catch (Exception e) {
            log.error("통계 페이지 로드 중 오류 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "통계 페이지를 불러오는 중 오류가 발생했습니다.");
        }
    }

    /**
     * AJAX API 요청 처리
     */
    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response,
                                  String pathInfo) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, Object> result = null;

            switch (pathInfo) {
                case "/api/monthly-sales":
                    result = handleMonthlySales(request);
                    log.info("=== 월별 매출 API 호출 ===");
                    log.info("year: {}", request.getParameter("year"));
                    log.info("month: {}", request.getParameter("month"));
                    log.info("includeMembership: {}", request.getParameter("includeMembership"));
                    break;

                case "/api/cumulative-sales":
                    result = handleCumulativeSales(request);
                    break;

                case "/api/car-type-stats":
                    result = handleCarTypeStats(request);
                    break;

                case "/api/peak-time":
                    result = handlePeakTime(request);
                    break;

                case "/api/member-stats":
                    result = handleMemberStats(request);
                    break;

                case "/api/today-summary":
                    result = handleTodaySummary(request);
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "API를 찾을 수 없습니다.");
                    return;
            }

            // JSON 응답
            String jsonResponse = objectMapper.writeValueAsString(result);
            PrintWriter out = response.getWriter();
            out.print(jsonResponse);
            out.flush();

            log.info("API 응답 완료: " + pathInfo);

        } catch (Exception e) {
            log.error("API 요청 처리 중 오류 발생: " + pathInfo, e);
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, Object> errorResponse = Map.of(
                    "error", true,
                    "message", e.getMessage(),
                    "cause", e.getClass().getName()
            );

            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(errorResponse));
            out.flush();
        }
    }

    /**
     * 월별 매출 통계 API
     */
    private Map<String, Object> handleMonthlySales(HttpServletRequest request) {
        int year = getIntParameter(request, "year", 2026);

        // 빈 문자열 체크 추가!
        String monthParam = request.getParameter("month");
        Integer month = null;

        if (monthParam != null && !monthParam.isEmpty() && !monthParam.equals("all")) {
            try {
                month = Integer.parseInt(monthParam);
            } catch (NumberFormatException e) {
                log.warn("잘못된 month 파라미터: {}", monthParam);
                month = null;
            }
        }

        boolean includeMembership = getBooleanParameter(request, "includeMembership", false);

        log.info("월별 매출 조회: year={}, month={}, includeMembership={}",
                year, month, includeMembership);

        return statisticService.getMonthlySales(year, month, includeMembership);
    }

    /**
     * 누적 매출 통계 API
     */
    private Map<String, Object> handleCumulativeSales(HttpServletRequest request) {
        int year = getIntParameter(request, "year", 2026);

        // 빈 문자열 체크 추가!
        String monthParam = request.getParameter("month");
        Integer month = null;

        if (monthParam != null && !monthParam.isEmpty() && !monthParam.equals("all")) {
            try {
                month = Integer.parseInt(monthParam);
            } catch (NumberFormatException e) {
                log.warn("잘못된 month 파라미터: {}", monthParam);
                month = null;
            }
        }

        boolean includeMembership = getBooleanParameter(request, "includeMembership", false);

        log.info("누적 매출 조회: year={}, month={}, includeMembership={}",
                year, month, includeMembership);

        return statisticService.getCumulativeSales(year, month, includeMembership);
    }

    /**
     * 차종별 통계 API
     */
    private Map<String, Object> handleCarTypeStats(HttpServletRequest request) {
        int year = getIntParameter(request, "year", 2026);

        // 빈 문자열 체크 추가
        String monthParam = request.getParameter("month");
        Integer month = null;

        if (monthParam != null && !monthParam.isEmpty() && !monthParam.equals("all")) {
            try {
                month = Integer.parseInt(monthParam);
            } catch (NumberFormatException e) {
                log.warn("잘못된 month 파라미터: {}", monthParam);
                month = null;
            }
        }

        log.info("차종별 통계 조회: year={}, month={}", year, month);

        return statisticService.getCarTypeStats(year, month);
    }

    /**
     * 피크 시간대 API
     */
    private Map<String, Object> handlePeakTime(HttpServletRequest request) {
        int year = getIntParameter(request, "year", LocalDate.now().getYear());

        String monthParam = request.getParameter("month");
        Integer month = null;
        if (monthParam != null && !monthParam.isEmpty() && !monthParam.equals("all")) {
            try {
                month = Integer.parseInt(monthParam);
            } catch (NumberFormatException e) {
                log.warn("잘못된 month 파라미터: {}", monthParam);
            }
        }

        log.info("피크 시간대 조회: year={}, month={}", year, month);
        return statisticService.getPeakTimeStats(year, month);
    }

    /**
     * 회원 통계 API
     */
    private Map<String, Object> handleMemberStats(HttpServletRequest request) {
        log.info("회원 통계 조회");
        return statisticService.getMemberStats();
    }

    /**
     * 오늘 요약 API
     */
    private Map<String, Object> handleTodaySummary(HttpServletRequest request) {
        log.info("오늘 요약 조회");
        return statisticService.getTodaySummary();
    }

    // ========================================
    // 유틸리티 메서드
    // ========================================

    /**
     * request에서 int 파라미터 추출
     */
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("잘못된 숫자 형식: {}={}", name, value);
            return defaultValue;
        }
    }

    /**
     * request에서 boolean 파라미터 추출
     */
    private boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
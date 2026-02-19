package com.example.smartparkingsystem.controller;

import com.example.smartparkingsystem.dto.ParkingHistoryDTO;
import com.example.smartparkingsystem.dto.PaymentHistoryDTO;
import com.example.smartparkingsystem.dto.PaymentInfoDTO;
import com.example.smartparkingsystem.service.ParkingHistoryService;
import com.example.smartparkingsystem.service.PaymentHistoryService;
import com.example.smartparkingsystem.service.PaymentInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet(name = "parkingController", value = "/parking/*")
public class ParkingController extends HttpServlet {
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;
    private final PaymentHistoryService paymentHistoryService = PaymentHistoryService.getInstance();
    private final PaymentInfoService paymentInfoService = PaymentInfoService.getInstance();

    // 입출차 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8"); // 응답 형식 JSON화 (js에서 읽을 수 있도록)
        String action = req.getPathInfo();

        switch (action) {
            case "/entry" -> handleEntry(req, resp);// 입차처리
            case "/exit" -> handleExit(req, resp); // 출차처리
            case "/payment" -> handlePayment(req, resp); // 결제 처리
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND); // 404 에러
        }
    }

    private void handleEntry(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String parkingArea = req.getParameter("parkingArea");
        String carNum = req.getParameter("carNum");
        String carType = req.getParameter("carType");

        try {
            ParkingHistoryDTO existing = parkingService.getRecentParking(carNum);

            // 이미 주차 중인 차량
            if (existing != null) {
                resp.getWriter().write("{\"success\": false, \"message\": \"이미 주차 중인 차량입니다.\"}");
                return;
            }

            // DB 저장
            ParkingHistoryDTO parkingHistoryDTO = ParkingHistoryDTO.builder()
                    .parkingArea(parkingArea).carNum(carNum).carType(carType).build();
            parkingService.registerEntry(parkingHistoryDTO);

            // 저장 정보 다시 가져오기
            ParkingHistoryDTO saved = parkingService.getRecentParking(carNum);

            if (saved == null) {
                // DB Insert는 됐는데 Select가 안되는 경우
                resp.setStatus(500);
                resp.getWriter().write("{\"success\": false, \"message\": \"DB 조회 실패\"}");
                return;
            }

            // 날짜 변환 및 JSON 전송
            String entryTimeStr = String.valueOf(saved.getEntryTime()).replace(" ", "T");
            resp.getWriter().write(
                    "{\"success\": true" +
                            ", \"entryTime\": \"" + entryTimeStr + "\"" +
                            ", \"parkNo\": " + saved.getParkNo() + "}"
            );

        } catch (Exception e) {
            log.error("입차 처리 중 진짜 에러 발생: ", e);
            resp.setStatus(500);
            try { resp.getWriter().write("{\"success\": false, \"message\": \"서버 내부 오류\"}"); } catch(Exception ex){}
        }
    }

    private void handleExit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        HttpSession session = req.getSession();
//        String adminId = (String) session.getAttribute("adminId");
//
//        if (adminId == null || adminId.trim().isEmpty()) {
//            resp.sendRedirect("/login");
//            return;
//        }

        // 1. main_modal.js에서 넘어온 값
        long parkNo = Long.parseLong(req.getParameter("parkNo"));

        // 2. DB 업데이트
        ParkingHistoryDTO parkingHistoryDTO = ParkingHistoryDTO.builder()
                .parkNo(parkNo).build();
        parkingService.registerExit(parkingHistoryDTO);

        // 3. JS한테 성공 응답 보내기
        try {
            resp.getWriter().write("{\"success\": true}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePayment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String parkNoStr = req.getParameter("parkNo");

        if (parkNoStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\": false, \"message\": \"parkNo가 필요합니다\"}");
            return;
        }

        Long parkNo = Long.valueOf(parkNoStr);
        ParkingHistoryDTO parkingHistoryDTO = parkingService.getParkingHistory(parkNo);
        if (parkingHistoryDTO == null) {
            resp.setStatus(404);
            resp.getWriter().write("{\"success\": false, \"message\": \"주차 정보를 찾을 수 없습니다\"}");
            return;
        }
        String carNum = parkingHistoryDTO.getCarNum();

        // 계산 후 테이블 등록
        paymentHistoryService.calculateFinalCharge(carNum);


        // 결제 정보 조회
        PaymentHistoryDTO paymentHistoryDTO = paymentHistoryService.getRecentPayment(carNum);
        PaymentInfoDTO paymentInfoDTO = paymentInfoService.getInfo();

        if (paymentHistoryDTO == null) {
            resp.setStatus(500);
            resp.getWriter().write("{\"success\": false, \"message\": \"결제 정보 조회 실패\"}");
            return;
        }

        // 날짜 변환 및 JSON 전송
        resp.getWriter().write(
                "{\"success\": true" +
//                            ", \"payNo\": \"" + paymentHistoryDTO.getPayNo() + "\"" +
                        ", \"carNum\": \"" + carNum + "\"" +
                        ", \"entryTime\": \"" + paymentHistoryDTO.getEntryTime() + "\"" +
                        ", \"exitTime\": \"" + paymentHistoryDTO.getExitTime() + "\"" +
                        ", \"totalMinutes\": \"" + paymentHistoryDTO.getTotalMinutes() + "\"" +
                        ", \"basicCharge\": \"" + paymentInfoDTO.getBasicCharge() + "\"" +
                        ", \"extraCharge\": \"" + (paymentHistoryDTO.getTotalCharge() - paymentInfoDTO.getBasicCharge()) + "\"" +
                        ", \"discountAmount\": \"" + paymentHistoryDTO.getDiscountAmount() + "\"" +
                        ", \"totalCharge\": " + paymentHistoryDTO.getTotalCharge() + "}"
        );
    }
}
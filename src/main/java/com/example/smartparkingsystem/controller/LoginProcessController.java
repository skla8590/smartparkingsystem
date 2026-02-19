package com.example.smartparkingsystem.controller;

import com.example.smartparkingsystem.dto.AdminDTO;
import com.example.smartparkingsystem.dto.ValidationDTO;
import com.example.smartparkingsystem.service.AdminService;
import com.example.smartparkingsystem.service.MailService;
import com.example.smartparkingsystem.service.ValidationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/login")
public class LoginProcessController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String step = req.getParameter("step");

        if (step == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 (값 자체가 없음, 요청 자체가 이상할때만)
            return;
        }

        switch (step) {
            case "1" -> step1(req, resp);
            case "2" -> step2(req, resp);
            case "3" -> step3(req, resp);
            default -> resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 (정보를 찾을 수 없음)
        }
    }

    // Step1 세션
    private void step1(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String adminId = req.getParameter("adminId");
        String password = req.getParameter("password");

        // 로그인 실패
        if (!adminDB(adminId, password)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 (실패)
            return;
        }

        // 사용여부 False
        if (!adminService.getAdminById(adminId).isActive()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 (접근 제한)
            return;
        }

        // 로그인 성공시 임시 세션생성
        HttpSession session = req.getSession();
        session.setAttribute("tempAdminId", adminId);
        resp.setStatus(HttpServletResponse.SC_OK); // 200 (승인)
    }

    // Step2 등록된 이메일 확인
    private void step2(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        HttpSession session = req.getSession();
        String tempAdminId = (String) session.getAttribute("tempAdminId");
        // TODO 이건 왜 만들었는지 기억이 안남
//        String otpCode = validationService.getOTP(tempAdminId).getOtpCode();

        // step1의 임시세션에 아이디 없으면 400에러
        if (tempAdminId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            return;
        }

        // 같은 레코드에 이메일인지 확인
        if (emailDB(tempAdminId, email)) {

            // Step2에서 Step3로 넘어오려면 인증하기 버튼을 눌러야하기 때문에
            // Step3로 들어갈때 바로 발송
            validationService.otpShipment(tempAdminId);

            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // Step3 OTP 확인
    private void step3(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String tempAdminId = (String) session.getAttribute("tempAdminId"); // 임시 세션
        String otpCode = req.getParameter("otpCode");
        System.out.println("Step3 otpCode: " + otpCode);

        String resultOTP = otpDB(tempAdminId, otpCode); // 문자열로 결과 받는 변수

        // OTP 완료시 임시 세션 변경
        if ("Success".equals(resultOTP)) {

            // adminId로 변경
            session.setAttribute("adminId", tempAdminId); // 최종 로그인 세션 적용
            session.removeAttribute("tempAdminId"); // 임시 세션 제거
            adminService.renewalLog(tempAdminId, req.getRemoteAddr()); // 로그인 날짜, IP
            String adminId = (String) session.getAttribute("adminId");

            System.out.println("Controller step3 adminId 세션 생성 완료: " + session.getId());
            System.out.println("Controller step3 adminId 값 " + adminId);

            if (adminService.getAdminById(adminId).isPasswordReset()) {
                resp.sendRedirect("/main/mypage");
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if ("Expired".equals(resultOTP)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else if ("Fail".equals(resultOTP)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private boolean adminDB(String adminId, String password) {
        return adminService.AuthenticateAdmin(adminId, password);
    }

    private boolean emailDB(String adminId, String email) {
        AdminDTO admin = adminService.getAdminById(adminId);
        return admin != null && admin.getAdminEmail().equals(email);
    }

    // OTP 인증, 발송 헬퍼 메서드
    private String otpDB(String adminId, String otpCode) {
        ValidationDTO validationDTO = validationService.getOTP(adminId);
//        String otp = validationDTO.getOtpCode();

        if (LocalDateTime.now().isAfter(validationDTO.getExpiredTime())) {
            return "Expired"; // 만료
        }

        // OTP 승인
        if (validationDTO.getOtpCode().equals(otpCode)) {
            return "Success";
        } else { // 실패
            return "Fail";
        }
    }
}
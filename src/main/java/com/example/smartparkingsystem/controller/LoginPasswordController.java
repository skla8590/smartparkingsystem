package com.example.smartparkingsystem.controller;

import com.example.smartparkingsystem.dto.AdminDTO;
import com.example.smartparkingsystem.dto.ValidationDTO;
import com.example.smartparkingsystem.service.AdminService;
import com.example.smartparkingsystem.service.ValidationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Log4j2
@WebServlet("/password")
public class LoginPasswordController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/login/password.jsp").forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String step = req.getParameter("step");

        if (step == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            return;
        }
        log.info("step: {}", step);
        switch (step) {
            case "1" -> step1(req, resp);
            case "2" -> step2(req, resp);
            case "3" -> step3(req, resp);
            default -> resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // DB에 아이디가 있는지 확인 (없어도 넘어감)
    public void step1(HttpServletRequest req, HttpServletResponse resp) {
        String adminId = req.getParameter("adminId");
        if (adminId == null || adminId.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        log.info("adminId: {}", adminId);

        if (adminIdValid(adminId)) {
            HttpSession session = req.getSession();
            session.setAttribute("logAdminId", adminId); // 임시 세션 생성
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // DB에 아이디가 있으면 해당하는 레코드에 이메일 확인 (Step1에서 부터 401응답을 받으면 무슨 값을 넣어도 에러)
    public void step2(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        String adminId = (String) session.getAttribute("logAdminId");
        String email = req.getParameter("email");
        log.info("step2 adminId: {}", adminId);
        log.info("email: {}", email);
        
        if (emailValid(adminId, email)) {
            session.setAttribute("logEmail", email);
            validationService.otpShipment(adminId); // 레코드에 등록된 이메일이 맞으면 바로 발송
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    public void step3(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();
        String adminId = (String) session.getAttribute("logAdminId");
//        String email = (String) session.getAttribute("logEmail");
        String otpCode = req.getParameter("otpCode");
        log.info("otpCode: {}", otpCode);

        String resultOTP = otpValid(adminId, otpCode);

        if ("Success".equals(resultOTP)) {

            // TODO 이것도 필요없을듯 제거
//            // 12자리 랜덤 UUID 생성
//            String newPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
//
//            // 랜덤키 변경후 최초 로그인 변경
//            AdminDTO adminDTO = AdminDTO.builder()
//                    .adminId(adminId)
//                    .password(newPassword)
//                    .adminEmail(email)
//                    .isPasswordReset(true) // 변경
//                    .build();
//            adminService.modifyAdmin(adminDTO);

            // 랜덤키 메일로 발송과 동시에 비밀번호 변경
            validationService.uuidPassword(adminId);

            session.removeAttribute("logAdminId");
            session.removeAttribute("logEmail");
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if ("Fail".equals(resultOTP)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    private boolean adminIdValid(String adminId) {
        return adminService.getAdminById(adminId) != null;
    }
    private boolean emailValid(String adminId, String email) {
        AdminDTO admin = adminService.getAdminById(adminId);
        return admin != null && admin.getAdminEmail().equals(email);
    }

    private String otpValid(String adminId, String otpCode) {
        ValidationDTO validationDTO = validationService.getOTP(adminId);

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

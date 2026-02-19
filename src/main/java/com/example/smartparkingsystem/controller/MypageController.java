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
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@WebServlet("/main/mypage")
public class MypageController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/login/myPage.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String box = req.getParameter("box");

        if (box == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (box) {
            case "1" -> box1(req, resp);
            case "2" -> box2(req, resp);
            case "returnOTP" -> returnOTP(req, resp); // OTP 재발송 따로 분리
            default -> resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // 첫번째 박스 (이메일 수정칸)
    private void box1 (HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        /*
        이메일 변경 구현 (팝업창 띄워서 이메일 인증하는 형식 + 4분 타이머 추가)
        OTP연결시 제한시간 4분
        */
        HttpSession session = req.getSession(false);
        String adminId = (String) session.getAttribute("adminId");
        String otpCode = req.getParameter("otpCode");
        String newEmail = req.getParameter("newEmail");

        log.info("Box1 New Email : {}", newEmail);
        log.info("Box1 OTP Code : {}", otpCode);

        // OTP발송 (인증하기 누르면 발송하도록 JS연결)
        if ((newEmail == null || newEmail.trim().isEmpty()) && (otpCode == null || otpCode.trim().isEmpty())) {
            validationService.otpShipment(adminId);
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // OTP코드 인증 여부
        if (newEmail == null) {
            String resultOTP = validateOtp(adminId, otpCode);

            if ("Success".equals(resultOTP)) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else if ("Expired".equals(resultOTP)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }


        // 이메일 변경
        if (otpCode == null) {
            AdminDTO adminDTO = AdminDTO.builder()
                    .adminId(adminId)
                    .password(adminService.getAdminById(adminId).getPassword())
                    .adminEmail(newEmail) // 변경
                    .isPasswordReset(false)
                    .build();
            adminService.modifyAdmin(adminDTO);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        }
    }

    // 두번째 박스 (비밀번호 변경 칸)
    private void box2 (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String adminId = (String) session.getAttribute("adminId");
        String password = req.getParameter("password");
        String newPassword = req.getParameter("newPassword");
        String newPasswordBCrypt = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String email = adminService.getAdminById(adminId).getAdminEmail();


        log.info("Box2 Admin id : {}", adminId);
        log.info("Box2 Password : {}", password);
        log.info("Box2 New Password : {}", newPassword);
        log.info("Box2 New Password BCrypt : {}", newPasswordBCrypt);
        log.info("Box2 Email : {}", email);


        if (password == null || newPassword == null) { // 굳이 필요할까?
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 암호화된 비밀번호와 사용자가 입력한 비밀번호 비교
        boolean checkPw = BCrypt.checkpw(password, adminService.getAdminById(adminId).getPassword());

        // DB비밀번호와 일치함
        if (checkPw) {

            AdminDTO adminDTO = AdminDTO.builder()
                    .adminId(adminId)
                    .password(newPasswordBCrypt) // 변경
                    .adminEmail(email)
                    .isPasswordReset(false) // 변경
                    .build();

            adminService.modifyAdmin(adminDTO);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        }
    }

    private void returnOTP (HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = (String) session.getAttribute("adminId");
        if (adminId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        validationService.otpShipment(adminId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // OTP 인증 헬퍼 메서드
    private String validateOtp(String adminId, String otpCode) {
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

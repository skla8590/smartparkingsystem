package com.example.smartparkingsystem.filter;

import com.example.smartparkingsystem.service.AdminService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        AdminService adminService = AdminService.INSTANCE;
        String uri = req.getRequestURI();
        String loginPath = "/login";
        String passwordPath = "/password";
        String logoutPath = "/logout";
        String mypagePath = "/main/mypage";
        String mypageEmail = "/web/emailVerification.jsp";

        // 속성 파일 필터 제외
        if (uri.endsWith(".css") || uri.endsWith(".js") ||
                uri.endsWith(".png") || uri.endsWith(".jpg") ||
                uri.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // 세션없이 뒤로가기 금지(캐시 저장금지, 재검증) = JS, CSS는 캐시해도 괜찮고 성능향상하기 때문에 제외
        if (!uri.startsWith("/login") && !uri.startsWith("/password") && !uri.endsWith(".css") && !uri.endsWith(".js")) {
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");
        }

        // 제외 폴더 지정 (로그인, 비밀번호 재설정, 로그아웃)
        if (uri.startsWith(loginPath) || uri.startsWith(passwordPath) || uri.startsWith(logoutPath) || uri.startsWith(mypageEmail)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            log.info("인증된 세션이 없음 접근 제한");
            resp.sendRedirect(loginPath);
            return;
        }

        // BUG 발생 로그아웃 안됨
        String adminId = (String) session.getAttribute("adminId");
        if (adminService.getAdminById(adminId).isPasswordReset()) {
            if (!uri.startsWith(mypagePath)) {
                log.info("비밀번호 재설정후 최초 로그인 이동제한");
                resp.sendRedirect(mypagePath);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

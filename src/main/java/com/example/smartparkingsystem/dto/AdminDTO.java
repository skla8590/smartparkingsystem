package com.example.smartparkingsystem.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private String adminId; // 관리자 아이디
    private String password; // 관리자 비밀번호
//    private String birth; // TODO 생년월일 (불필요 : DB제거)
    private String adminName; // 관리자 이름
    private String adminEmail; // 관리자 이메일
    private boolean isActive; // 활동 여부 (계정활동 허용, 비허용)
    private LocalDateTime lastLogin; // 마지막 로그인 날짜
    private String lastLoginIp; // 마지막 로그인 아이피
    private boolean isPasswordReset; // 최초 로그인 여부 True여부, False일반
//    private LocalDateTime created_at; // TODO 계정 생성일(사용하지 않아서 주석)
}

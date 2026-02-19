package com.example.smartparkingsystem.service;

import java.security.SecureRandom;
import com.example.smartparkingsystem.dao.ValidationDAO;
import com.example.smartparkingsystem.dto.AdminDTO;
import com.example.smartparkingsystem.dto.ValidationDTO;
import com.example.smartparkingsystem.util.MapperUtil;
import com.example.smartparkingsystem.vo.ValidationVO;
import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public enum ValidationService {
    INSTANCE;

    private ValidationDAO validationDAO;
    private AdminService adminService;
    private MailService mailService;
    private ModelMapper modelMapper;
    private SecureRandom random; // 랜덤값 생성 변경(시큐리티)

    ValidationService() {
        validationDAO = new ValidationDAO();
        adminService = AdminService.INSTANCE;
        modelMapper = MapperUtil.INSTANCE.getInstance();
        mailService = MailService.INSTANCE;
        random = new SecureRandom();
    }

    // 시큐리티 랜덤값 사용 (앞자리 0 포함 6자리)
    public String randomOTP() {
        int otpCode = random.nextInt(1000000);
        return String.format("%06d", otpCode);
    }

    // OTP 발송
    public void otpShipment(String adminId) {
        // 발송할 이메일
        String adminEmail = adminService.getAdminById(adminId).getAdminEmail();

        String otpCode = randomOTP();

        // TODO 테스트할때는 수정
        mailService.sendAuthEmail(adminEmail, otpCode);
        ValidationVO validationVO = ValidationVO.builder()
                .adminId(adminId)
                .otpCode(otpCode)
                .adminEmail(adminEmail)
                .expiredTime(LocalDateTime.now())
                .build();
        validationDAO.logOTP(validationVO);
    }

    // 재설정 비밀번호 발송
    public void uuidPassword(String adminId) {
        // 발송할 이메일
        String adminEmail = adminService.getAdminById(adminId).getAdminEmail();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String BCryptUuid = BCrypt.hashpw(uuid, BCrypt.gensalt(12));

        // TODO 테스트할때는 수정
        mailService.sendAuthPw(adminEmail, uuid);
        AdminDTO adminDTO = AdminDTO.builder()
                .adminId(adminId)
                .adminEmail(adminEmail)
                .password(BCryptUuid) // 랜덤키도 암호화 하여 DB저장
                .isPasswordReset(true)
                .build();
        adminService.modifyAdmin(adminDTO);

    }

    // 관리자 아이디로 OTP검색
    public ValidationDTO getOTP (String adminId) {
        return modelMapper.map(validationDAO.selectOTPOne(adminId), ValidationDTO.class);
    }
}

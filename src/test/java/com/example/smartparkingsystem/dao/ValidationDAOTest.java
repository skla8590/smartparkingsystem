package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.vo.ValidationVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@Log4j2
class ValidationDAOTest {
    private ValidationDAO validationDAO;

    @BeforeEach
    public void ready() {
        validationDAO = new ValidationDAO();
    }

    @Test
    public void LogOTPUpdate() {
        ValidationVO validationVO = ValidationVO.builder()
                .adminId("test1")
                .otpCode("123456")
                .adminEmail("test2@gmail.com")
                .expiredTime(LocalDateTime.now())
                .build();
        log.info(validationVO);
        validationDAO.logOTP(validationVO);
    }

    @Test
    public void LogOTPSelectOne() {
        log.info(validationDAO.selectOTPOne("test1"));
    }
}
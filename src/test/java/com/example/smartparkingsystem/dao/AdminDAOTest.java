package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.vo.AdminVO;
import com.example.smartparkingsystem.vo.ValidationVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class AdminDAOTest {
    private AdminDAO adminDAO;

    @BeforeEach
    public void ready() {
        adminDAO = new AdminDAO();
    }

    @Test
    public void AdminAll() {
        for (AdminVO adminVO : adminDAO.selectAllAdmin()) {
            log.info(adminVO);
        }
    }

    @Test
    public void AdminById() {
        log.info(adminDAO.selectAdminById("test"));
    }

    @Test
    public void AdminUpdate() {
        AdminVO adminVO = AdminVO.builder()
                .adminId("test1")
                .password("test1")
                .adminName("수정")
                .adminEmail("test1@gmail.com")
                .isActive(true)
                .isPasswordReset(false)
                .build();
        log.info(adminVO);
        adminDAO.updateAdmin(adminVO);
    }

    @Test
    public void test() {
        AdminVO adminVO = AdminVO.builder()
                .adminId("test1")
                .password("test1")
                .adminEmail("test1@nate.com")
                .isPasswordReset(true)
                .build();
        log.info(adminVO);
        adminDAO.updateAdmin(adminVO);
    }

    @Test
    public void LogUpdate() {
        String adminId = "test1";
        String lastLoginIp = "192.168.0.1";
        adminDAO.updateLog(adminId, lastLoginIp);
    }

//    @Test
//    public void AdminDelete() {
//        adminDAO.deleteAdmin("test1");
//    }
}
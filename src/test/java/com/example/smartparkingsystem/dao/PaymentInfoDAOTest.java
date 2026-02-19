package com.example.smartparkingsystem.dao;

import com.example.smartparkingsystem.vo.PaymentInfoVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
class PaymentInfoDAOTest {
    private PaymentInfoDAO paymentInfoDAO;

    @BeforeEach
    public void ready() {
        // 모든 테스트 전에 TodoDAO 타입의 객체를 생성
        paymentInfoDAO = new PaymentInfoDAO();
    }

    @Test
    public void selectInfo() {
        log.info(paymentInfoDAO.selectInfo());
    }

    @Test
    public void insertInfo() {
        PaymentInfoVO paymentInfoVO = PaymentInfoVO.builder()
                .freeTime(10)
                .basicTime(60)
                .extraTime(30)
                .basicCharge(3000)
                .extraCharge(1000)
                .maxCharge(15000)
                .memberCharge(100000)
                .smallCarDiscount(0.3)
                .disabledDiscount(0.5)
                .adminId("admin")
                .build();
        paymentInfoDAO.insertInfo(paymentInfoVO);
    }
}
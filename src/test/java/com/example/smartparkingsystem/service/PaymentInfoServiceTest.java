package com.example.smartparkingsystem.service;

import com.example.smartparkingsystem.dto.PaymentInfoDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
class PaymentInfoServiceTest {
    private PaymentInfoService paymentInfoService;

    @BeforeEach
    void ready() {
        paymentInfoService = PaymentInfoService.getInstance();
    }

    @Test
    void addInfoTest() {
        paymentInfoService.addInfo(PaymentInfoDTO.builder()
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
                .build());
    }

    @Test
    void getInfoTest() {
        PaymentInfoDTO paymentInfoDTO = paymentInfoService.getInfo();
        log.info(paymentInfoDTO);
    }
}
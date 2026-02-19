package com.example.smartparkingsystem.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class PaymentHistoryServiceTest {
    private PaymentHistoryService paymentHistoryService;

    @BeforeEach
    public void ready() {
        paymentHistoryService = PaymentHistoryService.getInstance();
    }

    @Test
    public void calculateFinalChargeTest() {
         paymentHistoryService.calculateFinalCharge("35가3946");
    }
}
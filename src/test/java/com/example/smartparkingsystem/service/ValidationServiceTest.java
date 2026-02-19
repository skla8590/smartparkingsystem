package com.example.smartparkingsystem.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class ValidationServiceTest {
    private ValidationService validationService;

    @BeforeEach
    public void ready() {
        validationService = ValidationService.INSTANCE;
    }

    @Test
    public void ShipmentTest() {
        validationService.otpShipment("test1");
    }

    @Test
    public void getOTPTest() {
        log.info(validationService.getOTP("test1"));
    }
}
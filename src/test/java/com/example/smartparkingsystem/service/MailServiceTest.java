package com.example.smartparkingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceTest {
    private MailService mailService;
    @BeforeEach
    void setUp() {
        mailService = MailService.getINSTANCE();
    }

    @Test
    void sendMail() {
        mailService.sendAuthEmail("vcg258@naver.com", "123456");
    }

}
package com.example.auction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    @Service
    @Slf4j
    class MockEmailService implements EmailService {
        @Override
        public void sendEmail(String to, String subject, String body) {
            log.info("[MOCK EMAIL] to={}, subject={}, body={}", to, subject, body);
        }
    }
}
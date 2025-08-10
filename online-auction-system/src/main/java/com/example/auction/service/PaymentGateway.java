package com.example.auction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface PaymentGateway {
    void charge(String accountReference, BigDecimal amount);

    @Service
    @Slf4j
    class MockPaymentGateway implements PaymentGateway {
        @Override
        public void charge(String accountReference, BigDecimal amount) {
            log.info("[MOCK PAYMENT] Charged {} for account {}", amount, accountReference);
        }
    }
}
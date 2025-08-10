package com.example.auction.config;

import com.example.auction.service.AuctionService;
import com.example.auction.service.EmailService;
import com.example.auction.service.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerConfig {
    private final AuctionService auctionService;
    private final PaymentGateway paymentGateway;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 30000)
    public void closeAuctions() {
        auctionService.closeExpiredAuctionsAndNotify(paymentGateway, emailService);
    }
}
package com.example.auction.service;

import com.example.auction.model.Auction;
import com.example.auction.model.Bid;
import com.example.auction.model.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.BidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AuctionServiceTest {

    private AuctionRepository auctionRepository;
    private BidRepository bidRepository;
    private SimpMessagingTemplate messagingTemplate;
    private AuctionService auctionService;

    @BeforeEach
    void setup() {
        auctionRepository = mock(AuctionRepository.class);
        bidRepository = mock(BidRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        auctionService = new AuctionService(auctionRepository, bidRepository, messagingTemplate);
    }

    @Test
    void placeBid_success() {
        Auction auction = Auction.builder()
                .id(1L).approved(true).closed(false)
                .startingPrice(new BigDecimal("100"))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(bidRepository.findMaxBidAmountByAuction(auction)).thenReturn(new BigDecimal("100"));
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
            Bid b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        User bidder = User.builder().id(2L).username("buyer").build();
        Bid saved = auctionService.placeBid(1L, bidder, new BigDecimal("120"));

        assertThat(saved.getAmount()).isEqualTo(new BigDecimal("120"));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/auction.1"), any(Object.class));
    }

    @Test
    void placeBid_reject_low_amount() {
        Auction auction = Auction.builder()
                .id(1L).approved(true).closed(false)
                .startingPrice(new BigDecimal("100"))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(bidRepository.findMaxBidAmountByAuction(auction)).thenReturn(new BigDecimal("150"));

        User bidder = User.builder().id(2L).username("buyer").build();
        assertThrows(IllegalArgumentException.class, () -> auctionService.placeBid(1L, bidder, new BigDecimal("150")));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
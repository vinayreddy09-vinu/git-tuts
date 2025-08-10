package com.example.auction.service;

import com.example.auction.model.Auction;
import com.example.auction.model.Bid;
import com.example.auction.model.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.BidRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<Auction> listPublicAuctions() {
        return auctionRepository.findByApprovedTrueAndClosedFalse();
    }

    public List<Auction> listPendingAuctions() {
        return auctionRepository.findByApprovedFalse();
    }

    public List<Auction> listClosedAuctions() {
        return auctionRepository.findByClosedTrue();
    }

    public List<Auction> listSellerAuctions(User seller) {
        return auctionRepository.findBySeller(seller);
    }

    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id);
    }

    @Transactional
    public Auction createAuction(Auction auction) {
        auction.setApproved(false);
        auction.setClosed(false);
        return auctionRepository.save(auction);
    }

    @Transactional
    public Auction updateAuction(Auction updated, User seller) {
        Auction existing = auctionRepository.findById(updated.getId())
                .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
        if (!existing.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("Not your auction");
        }
        if (existing.isApproved()) {
            throw new IllegalStateException("Cannot edit approved auction");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStartingPrice(updated.getStartingPrice());
        existing.setReservePrice(updated.getReservePrice());
        existing.setEndTime(updated.getEndTime());
        existing.setImageUrl(updated.getImageUrl());
        return auctionRepository.save(existing);
    }

    @Transactional
    public void deleteAuction(Long id, User seller) {
        Auction existing = auctionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
        if (!existing.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("Not your auction");
        }
        if (existing.isApproved()) {
            throw new IllegalStateException("Cannot delete approved auction");
        }
        auctionRepository.delete(existing);
    }

    @Transactional
    public Auction approveAuction(Long id) {
        Auction existing = auctionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
        existing.setApproved(true);
        return auctionRepository.save(existing);
    }

    @Transactional
    public Bid placeBid(Long auctionId, User bidder, BigDecimal amount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
        if (!auction.isApproved() || auction.isClosed()) {
            throw new IllegalStateException("Auction not open for bidding");
        }
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Auction has ended");
        }
        BigDecimal currentMax = bidRepository.findMaxBidAmountByAuction(auction);
        BigDecimal minAcceptable = currentMax != null ? currentMax : auction.getStartingPrice();
        if (amount.compareTo(minAcceptable) <= 0) {
            throw new IllegalArgumentException("Bid must be higher than current price");
        }
        Bid bid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();
        Bid saved = bidRepository.save(bid);

        messagingTemplate.convertAndSend("/topic/auction." + auctionId, new BidUpdateMessage(auctionId, amount, bidder.getUsername(), saved.getTimestamp(), "NEW_BID"));
        return saved;
    }

    @Transactional
    public void closeExpiredAuctionsAndNotify(PaymentGateway paymentGateway, EmailService emailService) {
        List<Auction> expired = auctionRepository.findByApprovedTrueAndClosedFalseAndEndTimeBefore(LocalDateTime.now());
        for (Auction auction : expired) {
            Optional<Bid> highest = bidRepository.findTopByAuctionOrderByAmountDesc(auction);
            auction.setClosed(true);
            auctionRepository.save(auction);
            if (highest.isPresent() && highest.get().getAmount().compareTo(auction.getReservePrice()) >= 0) {
                Bid winBid = highest.get();
                emailService.sendEmail(winBid.getBidder().getEmail(), "You won the auction: " + auction.getTitle(),
                        "Congratulations! Your bid of " + winBid.getAmount() + " won.");
                paymentGateway.charge(winBid.getBidder().getUsername(), winBid.getAmount());
                messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), new BidUpdateMessage(auction.getId(), winBid.getAmount(), winBid.getBidder().getUsername(), LocalDateTime.now(), "CLOSED_SOLD"));
            } else {
                messagingTemplate.convertAndSend("/topic/auction." + auction.getId(), new BidUpdateMessage(auction.getId(), null, null, LocalDateTime.now(), "CLOSED_UNSOLD"));
            }
        }
    }

    public record BidUpdateMessage(Long auctionId, BigDecimal amount, String bidder, LocalDateTime timestamp, String status) {}
}
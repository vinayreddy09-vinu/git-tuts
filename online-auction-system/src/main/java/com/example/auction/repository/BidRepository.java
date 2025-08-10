package com.example.auction.repository;

import com.example.auction.model.Auction;
import com.example.auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionOrderByAmountDesc(Auction auction);

    @Query("select max(b.amount) from Bid b where b.auction = :auction")
    BigDecimal findMaxBidAmountByAuction(Auction auction);

    Optional<Bid> findTopByAuctionOrderByAmountDesc(Auction auction);
}
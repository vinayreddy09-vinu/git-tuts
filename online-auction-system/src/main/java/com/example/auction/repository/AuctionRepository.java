package com.example.auction.repository;

import com.example.auction.model.Auction;
import com.example.auction.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByApprovedTrueAndClosedFalse();
    List<Auction> findByApprovedFalse();
    List<Auction> findBySeller(User seller);
    List<Auction> findByClosedTrue();
    List<Auction> findByApprovedTrueAndClosedFalseAndEndTimeBefore(LocalDateTime time);
}
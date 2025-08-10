package com.example.auction.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    private String imageUrl; // served from /uploads/

    @Column(nullable = false)
    private BigDecimal startingPrice;

    @Column(nullable = false)
    private BigDecimal reservePrice;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    private boolean approved = false;

    @Builder.Default
    private boolean closed = false;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();
}
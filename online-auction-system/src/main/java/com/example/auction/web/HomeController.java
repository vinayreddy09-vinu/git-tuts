package com.example.auction.web;

import com.example.auction.model.Auction;
import com.example.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuctionService auctionService;

    @GetMapping("/")
    public String index(Model model) {
        List<Auction> auctions = auctionService.listPublicAuctions();
        model.addAttribute("auctions", auctions);
        return "index";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("auctions", auctionService.listClosedAuctions());
        return "history";
    }
}
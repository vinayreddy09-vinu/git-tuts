package com.example.auction.web;

import com.example.auction.model.Auction;
import com.example.auction.model.User;
import com.example.auction.service.AuctionService;
import com.example.auction.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BuyerController {

    private final AuctionService auctionService;
    private final UserService userService;

    @GetMapping("/auction/{id}")
    public String viewAuction(@PathVariable Long id, Model model) {
        Auction auction = auctionService.findById(id).orElseThrow();
        model.addAttribute("auction", auction);
        model.addAttribute("bidForm", new BidForm());
        return "auction/detail";
    }

    @PostMapping("/auction/{id}/bid")
    @PreAuthorize("hasRole('BUYER')")
    public String placeBid(@PathVariable Long id, @ModelAttribute("bidForm") BidForm form,
                           @AuthenticationPrincipal UserDetails principal, Model model) {
        Auction auction = auctionService.findById(id).orElseThrow();
        User bidder = userService.findByUsername(principal.getUsername()).orElseThrow();
        try {
            auctionService.placeBid(id, bidder, form.getAmount());
        } catch (Exception e) {
            model.addAttribute("auction", auction);
            model.addAttribute("error", e.getMessage());
            return "auction/detail";
        }
        return "redirect:/auction/" + id + "?bidSuccess";
    }

    @Data
    public static class BidForm {
        @NotNull
        private BigDecimal amount;
    }
}
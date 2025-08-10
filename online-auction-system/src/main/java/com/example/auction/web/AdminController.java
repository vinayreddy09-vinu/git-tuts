package com.example.auction.web;

import com.example.auction.model.Auction;
import com.example.auction.repository.UserRepository;
import com.example.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuctionService auctionService;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    public String pendingAuctions(Model model) {
        model.addAttribute("auctions", auctionService.listPendingAuctions());
        return "admin/pending";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        auctionService.approveAuction(id);
        return "redirect:/admin/pending";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
}
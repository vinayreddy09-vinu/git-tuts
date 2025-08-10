package com.example.auction.web;

import com.example.auction.model.Auction;
import com.example.auction.model.User;
import com.example.auction.service.AuctionService;
import com.example.auction.service.FileStorageService;
import com.example.auction.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final AuctionService auctionService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/auctions")
    public String myAuctions(@AuthenticationPrincipal UserDetails principal, Model model) {
        User seller = userService.findByUsername(principal.getUsername()).orElseThrow();
        model.addAttribute("auctions", auctionService.listSellerAuctions(seller));
        return "seller/auctions";
    }

    @GetMapping("/auctions/new")
    public String newAuction(Model model) {
        model.addAttribute("form", new AuctionForm());
        return "seller/new";
    }

    @PostMapping("/auctions")
    public String create(@AuthenticationPrincipal UserDetails principal, @ModelAttribute("form") AuctionForm form,
                         @RequestParam("image") MultipartFile image, Model model) {
        User seller = userService.findByUsername(principal.getUsername()).orElseThrow();
        String imageUrl = fileStorageService.store(image);
        Auction auction = Auction.builder()
                .seller(seller)
                .title(form.getTitle())
                .description(form.getDescription())
                .startingPrice(form.getStartingPrice())
                .reservePrice(form.getReservePrice())
                .endTime(form.getEndTime())
                .imageUrl(imageUrl)
                .build();
        auctionService.createAuction(auction);
        return "redirect:/seller/auctions";
    }

    @GetMapping("/auctions/{id}/edit")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        Auction auction = auctionService.findById(id).orElseThrow();
        model.addAttribute("auction", auction);
        model.addAttribute("form", new AuctionForm(auction.getTitle(), auction.getDescription(), auction.getStartingPrice(), auction.getReservePrice(), auction.getEndTime()));
        return "seller/edit";
    }

    @PostMapping("/auctions/{id}")
    public String update(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, @ModelAttribute("form") AuctionForm form,
                         @RequestParam(value = "image", required = false) MultipartFile image) {
        User seller = userService.findByUsername(principal.getUsername()).orElseThrow();
        Auction existing = auctionService.findById(id).orElseThrow();
        String imageUrl = existing.getImageUrl();
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.store(image);
        }
        Auction updated = Auction.builder()
                .id(existing.getId())
                .seller(seller)
                .title(form.getTitle())
                .description(form.getDescription())
                .startingPrice(form.getStartingPrice())
                .reservePrice(form.getReservePrice())
                .endTime(form.getEndTime())
                .imageUrl(imageUrl)
                .build();
        auctionService.updateAuction(updated, seller);
        return "redirect:/seller/auctions";
    }

    @PostMapping("/auctions/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User seller = userService.findByUsername(principal.getUsername()).orElseThrow();
        auctionService.deleteAuction(id, seller);
        return "redirect:/seller/auctions";
    }

    @Data
    public static class AuctionForm {
        @NotBlank
        private String title;
        private String description;
        private BigDecimal startingPrice;
        private BigDecimal reservePrice;
        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endTime;

        public AuctionForm() {}
        public AuctionForm(String title, String description, BigDecimal startingPrice, BigDecimal reservePrice, LocalDateTime endTime) {
            this.title = title;
            this.description = description;
            this.startingPrice = startingPrice;
            this.reservePrice = reservePrice;
            this.endTime = endTime;
        }
    }
}
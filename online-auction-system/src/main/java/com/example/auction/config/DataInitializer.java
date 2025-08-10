package com.example.auction.config;

import com.example.auction.model.Auction;
import com.example.auction.model.Role;
import com.example.auction.model.User;
import com.example.auction.repository.AuctionRepository;
import com.example.auction.repository.RoleRepository;
import com.example.auction.repository.UserRepository;
import com.example.auction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            roleRepository.save(Role.builder().name("ROLE_SELLER").build());
            roleRepository.save(Role.builder().name("ROLE_BUYER").build());

            if (userRepository.findByUsername("admin").isEmpty()) {
                userService.registerUser("admin", "admin@example.com", "password", Set.of("ROLE_ADMIN"));
            }
            if (userRepository.findByUsername("seller").isEmpty()) {
                userService.registerUser("seller", "seller@example.com", "password", Set.of("ROLE_SELLER"));
            }
            if (userRepository.findByUsername("buyer").isEmpty()) {
                userService.registerUser("buyer", "buyer@example.com", "password", Set.of("ROLE_BUYER"));
            }

            User seller = userRepository.findByUsername("seller").orElseThrow();
            if (auctionRepository.count() == 0) {
                auctionRepository.save(Auction.builder()
                        .seller(seller)
                        .title("Vintage Watch")
                        .description("A classic vintage watch in great condition.")
                        .startingPrice(new BigDecimal("100"))
                        .reservePrice(new BigDecimal("150"))
                        .endTime(LocalDateTime.now().plusHours(6))
                        .imageUrl(null)
                        .approved(true)
                        .closed(false)
                        .build());
                auctionRepository.save(Auction.builder()
                        .seller(seller)
                        .title("Antique Vase")
                        .description("Beautiful antique vase from 19th century.")
                        .startingPrice(new BigDecimal("200"))
                        .reservePrice(new BigDecimal("300"))
                        .endTime(LocalDateTime.now().plusHours(12))
                        .imageUrl(null)
                        .approved(true)
                        .closed(false)
                        .build());
            }
        };
    }
}
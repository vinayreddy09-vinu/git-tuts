# Online Auction System

Tech: Spring Boot 3, Spring Data JPA, Spring Security, WebSocket (STOMP), Thymeleaf, Bootstrap, H2, JUnit.

## Run locally
- Prerequisites: Java 21+, Maven 3.9+
- Start app:

```
mvn spring-boot:run
```

App runs at `http://localhost:8080`

H2 Console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:auctiondb`)

## Sample users
- admin/password (ROLE_ADMIN)
- seller/password (ROLE_SELLER)
- buyer/password (ROLE_BUYER)

## Features
- Registration/login, roles: ADMIN, SELLER, BUYER
- Seller CRUD for auctions with image upload
- Admin approval workflow
- Buyer real-time bidding via WebSocket; live updates on auction page
- Scheduled closure: determines winner, mock email + payment
- History page for closed auctions
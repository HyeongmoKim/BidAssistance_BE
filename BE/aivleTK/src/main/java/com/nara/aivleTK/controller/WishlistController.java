package com.nara.aivleTK.controller;

import com.nara.aivleTK.dto.bid.BidResponse; // import 경로 수정
import com.nara.aivleTK.service.bid.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleWishlist(@RequestParam Integer userId, @RequestParam Integer bidId) {
        return ResponseEntity.ok(wishlistService.toggleWishlist(userId, bidId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BidResponse>> getUserWishlist(@PathVariable Integer userId) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }
}
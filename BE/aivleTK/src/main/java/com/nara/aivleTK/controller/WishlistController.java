package com.nara.aivleTK.controller;

import com.nara.aivleTK.dto.ApiResponse;
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

    private final JwtUtil jwtUtil;

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<String>> toggleWishlist(@RequestParam Integer bidId,
            @CookieValue(value = JwtUtil.AUTHORIZATION_HEADER, required = false) String tokenValue) {
        if (tokenValue == null) {
            throw new com.nara.aivleTK.exception.UnauthorizedException("로그인이 필요합니다.");
        }
        String token = jwtUtil.substringToken(tokenValue);
        if (!jwtUtil.validateToken(token)) {
            throw new com.nara.aivleTK.exception.UnauthorizedException("유효하지 않은 토큰입니다.");
        }
        int userId = jwtUtil.getUserInfoFromToken(token).get("user_id", Integer.class);

        return ResponseEntity.ok(ApiResponse.success(wishlistService.toggleWishlist(userId, bidId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BidResponse>>> getUserWishlist(
            @CookieValue(value = JwtUtil.AUTHORIZATION_HEADER, required = false) String tokenValue) {
        if (tokenValue == null) {
            throw new com.nara.aivleTK.exception.UnauthorizedException("로그인이 필요합니다.");
        }
        String token = jwtUtil.substringToken(tokenValue);
        if (!jwtUtil.validateToken(token)) {
            throw new com.nara.aivleTK.exception.UnauthorizedException("유효하지 않은 토큰입니다.");
        }
        int userId = jwtUtil.getUserInfoFromToken(token).get("user_id", Integer.class);

        return ResponseEntity.ok(ApiResponse.success(wishlistService.getUserWishlist(userId)));
    }
}
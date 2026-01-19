package com.nara.aivleTK.controller;

import com.nara.aivleTK.domain.BidLog;
import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.service.bid.BidLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/bid-logs")
@RequiredArgsConstructor
public class BidLogController {

    private final BidLogService bidLogService;

    // 1. 입찰 기록 저장 API
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> recordBid(
            @RequestParam Integer userId,
            @RequestParam Integer bidId, // Service와 동일하게 Integer 사용
            @RequestParam BigInteger price) {

        bidLogService.recordBid(userId, bidId, price);
        return ResponseEntity.ok(ApiResponse.success("입찰 내역이 성공적으로 기록되었습니다."));
    }

    // 2. 특정 유저의 입찰 내역 조회 API
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<BidLog>>> getUserLogs(@PathVariable Integer userId) {
        List<BidLog> logs = bidLogService.getUserBidLogs(userId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
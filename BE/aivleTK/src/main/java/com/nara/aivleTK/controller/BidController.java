package com.nara.aivleTK.controller;

import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.dto.bid.BidResponse;
import com.nara.aivleTK.service.bid.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "organization", required = false) String organization) {
        List<BidResponse> bids = bidService.searchBid(name, region, organization);
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @GetMapping("/{bidId}")
    public ResponseEntity<ApiResponse<BidResponse>> detailBids(@PathVariable int bidId) {
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}

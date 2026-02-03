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
    private final com.nara.aivleTK.service.bid.RecommendationService recommendationService;
    private final com.nara.aivleTK.service.bid.BidLogService bidLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "organization", required = false) String organization) {
        List<BidResponse> bids = (isBlank(name) && isBlank(region) && isBlank(organization))
                ? bidService.getAllBid()
                : bidService.searchBid(name, region, organization);

        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidsBatch(@RequestParam List<Integer> ids) {
        List<BidResponse> bids = bidService.getBidsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(bids));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getRecommendations(@RequestParam Integer userId) {
        List<BidResponse> list = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<ApiResponse<Void>> logBidView(@PathVariable Integer id, @RequestParam Integer userId) {
        bidLogService.logView(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @GetMapping("/{bidId:\\d+}")
    public ResponseEntity<ApiResponse<BidResponse>> detailBids(@PathVariable int bidId) {
        BidResponse response = bidService.getBidById(bidId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}

package com.nara.aivleTK.controller;


import com.nara.aivleTK.dto.BidResponse;
import com.nara.aivleTK.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    //도서 목록 조회 및 제목검색시 제목검색
    @GetMapping
    public ResponseEntity<List<BidResponse>> getBids(
            @RequestParam(name = "name",required = false)String name){
        List<BidResponse> bids = bidService.searchBid(name);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidResponse> detailBids(@PathVariable Long id){
        BidResponse response = bidService.getBidById(id);
        return ResponseEntity.ok(response);
    }

}

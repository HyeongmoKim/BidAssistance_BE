package com.nara.aivleTK.dto.bid;

import com.nara.aivleTK.domain.Bid;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Builder
public class BidAnalysisRequestDto {
    private int bidId;
    private String bidRealId;
    private BigInteger basicPrice;
    private BigInteger estimatePrice;
    private Double successBidRate;
    private Double bidRange;

    public static BidAnalysisRequestDto from(Bid bid){
        return BidAnalysisRequestDto.builder()
                .bidId(bid.getBidId())
                .bidRealId(bid.getBidRealId())
                .basicPrice(bid.getBasicPrice())
                .estimatePrice(bid.getEstimatePrice())
                .successBidRate(bid.getSuccessBidRate())
                .bidRange(bid.getBidRange())
                .build();
    }
}

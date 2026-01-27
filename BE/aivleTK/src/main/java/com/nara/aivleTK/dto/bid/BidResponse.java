package com.nara.aivleTK.dto.bid;

import com.nara.aivleTK.domain.Attachment.Attachment;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.AnalysisResultDto;
import com.nara.aivleTK.dto.board.AttachmentResponse;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Setter
@Builder
@AllArgsConstructor
public class BidResponse {
    private int id;
    private String realId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime openDate;
    private String region;
    private String organization;
    private String bidURL;
    private String bidReportURL;
    private BigInteger estimatePrice;
    private Double minimumBidRate;
    private AnalysisResultDto analysisResult;
    private BidDetailDto bidDetail;
    private List<AttachmentResponse> attachments;

    public BidResponse(Bid bid) {
        this.id = bid.getBidId();
        this.realId = bid.getBidRealId();
        this.name = bid.getName();
        this.startDate = bid.getStartDate();
        this.endDate = bid.getEndDate();
        this.openDate = bid.getOpenDate();
        this.region = bid.getRegion();
        this.organization = bid.getOrganization();
        this.bidURL = bid.getBidURL();
        this.estimatePrice = bid.getEstimatePrice();
        this.minimumBidRate = bid.getMinimumBidRate();
        this.attachments = bid.getAttachments().stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());
    }
}

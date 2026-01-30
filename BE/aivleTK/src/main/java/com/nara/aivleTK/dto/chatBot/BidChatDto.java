package com.nara.aivleTK.dto.chatBot;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BidChatDto {
    private int bidId;                 // 내부 식별용(선택)
    private String bidRealId;          // 공고번호
    private String name;               // 공고명
    private String region;             // 지역
    private String organization;       // 기관

    private LocalDateTime startDate;   // 시작일
    private LocalDateTime endDate;     // 마감일
    private LocalDateTime openDate;    // 개찰일

    private Long basicPrice;           // 기초금액(원)
    private Long estimatePrice;        // 추정가격(원)
    private Double minimumBidRate;
    private Double bidRange;

}

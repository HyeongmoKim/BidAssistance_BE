package com.nara.aivleTK.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int bidId;
    @Column
    private String bidRealId;
    @Column
    private String name;
    @Column
    private LocalDateTime startDate;
    @Column(nullable=true)
    private LocalDateTime endDate;
    @Column
    private LocalDateTime openDate;
    @Column
    private String region;
    @Column
    private String organization;
    @Column
    private String bidFileName;
    @Column(name = "bid_URL")
    private String bidURL;
    @Column(name = "bid_report_URL")
    private String bidReportURL;
    @Column
    private BigInteger estimatePrice; // 추정가격
    @Column
    private BigInteger basicPrice;    // 기초금액
    @Column
    private Double successBidRate;    // 낙찰하한율
    @Column
    private Double bidRange;          // 투찰범위 (새로 추가됨)
}

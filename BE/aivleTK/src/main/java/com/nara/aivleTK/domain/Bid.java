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
    @Column(name = "bid_URL")
    private String bidURL;
    @Column(name = "bid_report_URL")
    private String bidReportURL;
    @Column(name = "bid_report_Name")
    private String bidReportName;
    @Column(name = "bid_report_URL2",nullable=true)
    private String bidReportURL2;
    @Column(name = "bid_report_Name2",nullable=true)
    private String bidReportName2;
    @Column(name = "bid_report_URL3",nullable=true)
    private String bidReportURL3;
    @Column(name = "bid_report_Name3",nullable=true)
    private String bidReportName3;
    @Column(name = "bid_report_URL4",nullable=true)
    private String bidReportURL4;
    @Column(name = "bid_report_Name4",nullable=true)
    private String bidReportName4;
    @Column
    private BigInteger estimatePrice; // 추정가격
    @Column
    private BigInteger basicPrice;    // 기초금액
    @Column
    private Double minimumBidRate;    // 낙찰하한율
    @Column
    private Double bidRange;          // 투찰범위 (새로 추가됨)
}

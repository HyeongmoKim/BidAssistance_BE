package com.nara.aivleTK.dto;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultDto {
    private Integer bidBidId;
    private BigDecimal goldenRate;
    private Long predictPrice;
    private BigDecimal avgRate;
    private String filepath;
    private String analysisContent;
    private String contractMethod;
    private String trackRecord;
    private String qualification;
}

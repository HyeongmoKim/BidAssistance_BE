package com.nara.aivleTK.dto;

import com.nara.aivleTK.domain.AnalysisResult;
import com.nara.aivleTK.domain.Attachment.Attachment;
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
    private String analysisContent;
    private String pdfUrl;

    public static AnalysisResultDto from(AnalysisResult entity) {
        return AnalysisResultDto.builder()
                .goldenRate(entity.getGoldenRate())
                .predictPrice(entity.getPredictedPrice())
                .avgRate(entity.getAvgRate())
                .analysisContent(entity.getAnalysisContent())
                .pdfUrl(entity.getPdfUrl())
                .build();
    }
}

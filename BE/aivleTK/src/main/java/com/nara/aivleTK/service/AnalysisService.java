package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.AnalysisResult;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.AnalysisResultDto;
import com.nara.aivleTK.repository.AnalysisResultRepository;
import com.nara.aivleTK.repository.BidRepository;
//  추가: BidDetail 관련 import
import com.nara.aivleTK.domain.BidDetail;
import com.nara.aivleTK.repository.BidDetailRepository;
//  추가: FastAPI 통신 DTO import
import com.nara.aivleTK.dto.fastapi.FastApiAnalyzeRequest;
import com.nara.aivleTK.dto.fastapi.FastApiAnalyzeResponse;
//  추가: 로깅, 에러처리 import
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisResultRepository analysisResultRepository;
    private final BidRepository bidRepository;
    private final BidDetailRepository bidDetailRepository;
    private final WebClient webClient;

    /**
     * FastAPI로 공고 분석 요청 후 결과 저장 (비동기)
     */
    @Transactional
    public void analyzeAndSave(Integer bidId) {
        try {
            log.info("🔍 분석 시작 - Bid ID: {}", bidId);

            // 1. Bid 조회
            Bid bid = bidRepository.findById(bidId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid bid ID: " + bidId));

            // 2. BidDetail 조회 (23개 컬럼)
            BidDetail bidDetail = bidDetailRepository.findByBidBidId(bidId).orElse(null);

            // 3. Bid + BidDetail을 텍스트로 변환
            String analysisText = buildAnalysisText(bid, bidDetail);

            log.debug("📄 분석 텍스트:\n{}", analysisText);

            // 4. FastAPI 요청 생성
            FastApiAnalyzeRequest request = FastApiAnalyzeRequest.builder()
                    .text(analysisText)
                    .threadId("bid_" + bidId)
                    .build();

            // 5. FastAPI 호출
            FastApiAnalyzeResponse response = webClient.post()
                    .uri("/analyze")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("❌ FastAPI 오류 - Status: {}", clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("❌ 오류 내용: {}", errorBody);
                                            return Mono.error(new RuntimeException("FastAPI 오류: " + errorBody));
                                        });
                            }
                    )
                    .bodyToMono(FastApiAnalyzeResponse.class)
                    .block();

            if (response == null || response.getPrediction() == null) {
                throw new IllegalArgumentException("AI 서버로부터 응답 없음");
            }

            log.info("✅ FastAPI 응답 수신 - 예측가: {}", response.getPrediction().getPointEstimate());

            // 6. 분석 결과를 DB에 저장
            saveAnalysisResult(bidId, response);

            log.info("✅ 분석 완료 및 저장 성공 - Bid ID: {}", bidId);

        } catch (Exception e) {
            log.error("❌ 분석 실패 - Bid ID: {}, 오류: {}", bidId, e.getMessage(), e);
            throw new RuntimeException("분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // ========================================
    // Private 메서드들
    // ========================================

    /**
     * Bid + BidDetail을 분석용 텍스트로 변환
     */
    private String buildAnalysisText(Bid bid, BidDetail bidDetail) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 기본 공고 정보
        sb.append("=== 입찰 공고 정보 ===\n");
        sb.append("공고번호: ").append(bid.getBidRealId()).append("\n");
        sb.append("공고명: ").append(bid.getName()).append("\n");
        sb.append("발주기관: ").append(bid.getOrganization()).append("\n");
        sb.append("지역: ").append(bid.getRegion() != null ? bid.getRegion() : "전국").append("\n");
        sb.append("추정가격: ").append(bid.getPrice()).append("원\n");

        if (bid.getStartDate() != null) {
            sb.append("입찰시작: ").append(bid.getStartDate().format(formatter)).append("\n");
        }
        if (bid.getEndDate() != null) {
            sb.append("입찰마감: ").append(bid.getEndDate().format(formatter)).append("\n");
        }
        if (bid.getOpenDate() != null) {
            sb.append("개찰일시: ").append(bid.getOpenDate().format(formatter)).append("\n");
        }

        // BidDetail 정보 추가
        if (bidDetail != null) {
            sb.append("\n=== 상세 분석 정보 ===\n");

            // 금액 정보
            appendIfNotNull(sb, "기초금액", bidDetail.getBaseAmount(), "원");
            appendIfNotNull(sb, "추정가격", bidDetail.getEstimatedPrice(), "원");
            appendIfNotNull(sb, "예가범위", bidDetail.getEstimatedPriceRange(), "%");
            appendIfNotNull(sb, "낙찰하한율", bidDetail.getMinBidRate(), "%");
            appendIfNotNull(sb, "예산대비추정가", bidDetail.getBudgetToEstimateRatio(), "%");
            appendIfNotNull(sb, "순공사비", bidDetail.getNetConstructionCost(), "원");
            appendIfNotNull(sb, "낙찰가", bidDetail.getAwardPrice(), "원");

            // 비율/계수
            appendIfNotNull(sb, "난이도계수", bidDetail.getDifficultyCoefficient(), "");
            appendIfNotNull(sb, "안전관리비비율", bidDetail.getSafetyManagementFeeRatio(), "%");
            appendIfNotNull(sb, "품질관리비비율", bidDetail.getQualityManagementFeeRatio(), "%");
            appendIfNotNull(sb, "관급비비중", bidDetail.getGovernmentSuppliedMaterialRatio(), "%");
            appendIfNotNull(sb, "VAT비율", bidDetail.getVatRatio(), "%");

            // 기간
            appendIfNotNull(sb, "입찰준비기간", bidDetail.getBidPreparationPeriod(), "일");
            appendIfNotNull(sb, "공고개찰기간", bidDetail.getAnnouncementToOpeningPeriod(), "일");
            appendIfNotNull(sb, "자격등록기간", bidDetail.getQualificationRegistrationPeriod(), "일");

            // Boolean 플래그
            appendBooleanIfNotNull(sb, "지역의무공동계약", bidDetail.getRegionalJointContractRequired());
            appendBooleanIfNotNull(sb, "난이도계수 적용", bidDetail.getDifficultyCoefficientApplied());
            appendBooleanIfNotNull(sb, "안전관리비 적용", bidDetail.getSafetyManagementFeeApplied());
            appendBooleanIfNotNull(sb, "품질관리비 적용", bidDetail.getQualityManagementFeeApplied());
            appendBooleanIfNotNull(sb, "관급비 적용", bidDetail.getGovernmentSuppliedMaterialApplied());
            appendBooleanIfNotNull(sb, "VAT 적용", bidDetail.getVatApplied());
            appendBooleanIfNotNull(sb, "서울 광역권", bidDetail.getSeoulMetropolitanArea());
            appendBooleanIfNotNull(sb, "순공사비 결측", bidDetail.getNetConstructionCostMissing());
        }

        return sb.toString();
    }

    /**
     * 값이 null이 아니면 StringBuilder에 추가
     */
    private void appendIfNotNull(StringBuilder sb, String label, Object value, String unit) {
        if (value != null) {
            sb.append(label).append(": ").append(value).append(unit).append("\n");
        }
    }

    /**
     * Boolean 값 추가
     */
    private void appendBooleanIfNotNull(StringBuilder sb, String label, Boolean value) {
        if (value != null) {
            sb.append(label).append(": ").append(value ? "예" : "아니오").append("\n");
        }
    }

    /**
     * FastAPI 응답을 DB에 저장
     * ⭐ PDF 경로 저장 로직 추가됨
     */
    private void saveAnalysisResult(Integer bidId, FastApiAnalyzeResponse response) {
        FastApiAnalyzeResponse.PredictionResult prediction = response.getPrediction();

        // 기존 분석 결과가 있으면 업데이트, 없으면 생성
        AnalysisResult entity = analysisResultRepository.findByBidBidId(bidId)
                .orElse(new AnalysisResult());

        entity.setBidBidId(bidId);
        entity.setPredictedPrice(prediction.getPointEstimate());
        entity.setAnalysisContent(response.getReport()); // 마크다운 리포트
        entity.setAnalysisDate(LocalDateTime.now());

        // ⭐⭐⭐ PDF 경로 저장 추가 (새로 추가된 부분) ⭐⭐⭐
        if (response.getPdfPath() != null && !response.getPdfPath().isEmpty()) {
            entity.setFilepath(response.getPdfPath());
            log.info("📄 PDF 경로 저장: {}", response.getPdfPath());
        } else {
            log.warn("⚠️ PDF 경로가 FastAPI 응답에 없습니다");
        }
        // ⭐⭐⭐ 여기까지 새로 추가된 부분 ⭐⭐⭐

        // 추가 정보 (예: 신뢰도를 goldenRate에 매핑)
        BigDecimal confidenceRate = convertConfidenceToRate(prediction.getConfidence());
        entity.setGoldenRate(confidenceRate);

        // 예측 범위의 평균을 avgRate로 저장 (예시)
        if (prediction.getPredictedMin() != null && prediction.getPredictedMax() != null) {
            double avg = (prediction.getPredictedMax() - prediction.getPredictedMin()) / 2.0;
            entity.setAvgRate(BigDecimal.valueOf(avg / prediction.getPointEstimate() * 100)); // 변동폭 %
        }

        analysisResultRepository.save(entity);
        log.info("💾 분석 결과 저장 완료 - Bid ID: {}", bidId);
    }

    /**
     * 신뢰도 문자열을 숫자로 변환
     */
    private BigDecimal convertConfidenceToRate(String confidence) {
        if (confidence == null) return BigDecimal.valueOf(0.5);

        return switch (confidence.toLowerCase()) {
            case "high" -> BigDecimal.valueOf(1.0);
            case "medium" -> BigDecimal.valueOf(0.7);
            case "low" -> BigDecimal.valueOf(0.4);
            default -> BigDecimal.valueOf(0.5);
        };
    }
}
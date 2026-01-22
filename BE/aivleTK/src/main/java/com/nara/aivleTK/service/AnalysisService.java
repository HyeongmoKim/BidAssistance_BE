package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.AnalysisResult;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.AnalysisResultDto;
import com.nara.aivleTK.dto.bid.BidAnalysisRequestDto;
import com.nara.aivleTK.repository.AnalysisResultRepository;
import com.nara.aivleTK.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {
    private final AnalysisResultRepository analysisResultRepository;
    private final BidRepository bidRepository;
    private final WebClient webClient;
    private final AttachmentService attachmentService;

    @Async
    @Transactional
    public void analyzeAndSave(Integer bidId) {
        // 1. 공고 조회 (DB에서 먼저 가져옴)
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid bid ID: " + bidId));

        try {
            // 2. 요청 데이터 포장 (수정된 DTO 사용)
            BidAnalysisRequestDto requestDto = BidAnalysisRequestDto.from(bid);

            // 3. AI 서버 요청
            AnalysisResultDto response = webClient.post()
                    .uri("/predict") // AI 서버 엔드포인트
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(AnalysisResultDto.class)
                    .block(); // Async 내부이므로 block 사용 가능

            if (response == null) {
                throw new IllegalStateException("AI 서버 응답이 비어있습니다.");
            }

            // 4. 결과 저장
            AnalysisResult entity = new AnalysisResult();

            // AI 응답값이 아니라, 내가 조회한 bid 객체를 직접 넣어줍니다.
            entity.setBid(bid);

            // AI가 준 결과값 매핑
            entity.setGoldenRate(response.getGoldenRate());
            entity.setPredictedPrice(response.getPredictPrice());
            entity.setAvgRate(response.getAvgRate());
            entity.setAnalysisContent(response.getAnalysisContent());
            entity.setPdfUrl(response.getPdfUrl());


            analysisResultRepository.save(entity);
            log.info("AI 분석 결과 저장 완료 [공고번호: {}]", bid.getBidRealId());

        }
        catch (WebClientRequestException e) {
            // AI 서버가 꺼져있을 때 여기서 잡힘
            log.warn("AI 서버 연결 실패 (분석 건너뜀) - 공고번호: {} / 원인: 파이썬 서버가 켜져있는지 확인하세요.", bid.getBidRealId());
        } catch (Exception e) {
            log.error("AI 분석 중 알 수 없는 오류 발생 - 공고번호: {}", bid.getBidRealId(), e);
        }
    }
}
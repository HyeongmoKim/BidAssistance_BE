package com.nara.aivleTK.controller;

import com.nara.aivleTK.domain.AnalysisResult;
import com.nara.aivleTK.repository.AnalysisResultRepository;
import com.nara.aivleTK.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
@Slf4j  // ⭐ 로깅 추가
public class AnalysisResultController {

    private final AnalysisService analysisService;
    private final AnalysisResultRepository analysisResultRepository;  // ⭐ 추가

    // 1. 분석 요청 API (프론트에서 '분석하기' 버튼 클릭 시 호출)
    // POST /api/analysis/predict/10 (공고 ID 10번 분석 요청)
    @PostMapping("/predict/{bidId}")
    public ResponseEntity<String> performAnalysis(@PathVariable Integer bidId) {
        try {
            analysisService.analyzeAndSave(bidId);
            return ResponseEntity.ok("분석이 완료되었습니다. PDF 확인 주소: /api/analysis/pdf/" + bidId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("분석 중 오류 발생: " + e.getMessage());
        }
    }

    // ⭐⭐⭐ 여기부터 새로 추가되는 메서드들 ⭐⭐⭐

    /**
     * 2. 분석 결과 조회 API
     * GET /api/analysis/result/{bidId}
     */
    @GetMapping("/result/{bidId}")
    public ResponseEntity<?> getAnalysisResult(@PathVariable Integer bidId) {
        try {
            AnalysisResult result = analysisResultRepository.findByBidBidId(bidId)
                    .orElseThrow(() -> new RuntimeException("분석 결과를 찾을 수 없습니다."));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("분석 결과 조회 실패 - Bid ID: {}", bidId, e);
            return ResponseEntity.status(404).body("분석 결과 없음: " + e.getMessage());
        }
    }

    /**
     * 3. PDF 다운로드/보기 API
     * GET /api/analysis/pdf/{bidId}
     */
    @GetMapping("/pdf/{bidId}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Integer bidId) {
        try {
            log.info("📄 PDF 요청 - Bid ID: {}", bidId);

            // DB에서 분석 결과 조회
            AnalysisResult result = analysisResultRepository.findByBidBidId(bidId)
                    .orElseThrow(() -> new RuntimeException("분석 결과를 찾을 수 없습니다."));

            String pdfPath = result.getFilepath();
            log.info("📂 DB에 저장된 경로: {}", pdfPath);

            // PDF 경로 확인
            if (pdfPath == null || pdfPath.isEmpty()) {
                log.error("❌ PDF 경로가 DB에 없습니다 - Bid ID: {}", bidId);
                return ResponseEntity.status(404).body(null);
            }

            // 파일 존재 확인
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                log.error("❌ PDF 파일이 존재하지 않습니다: {}", pdfPath);
                log.error("   파일 절대 경로: {}", pdfFile.getAbsolutePath());
                return ResponseEntity.status(404).body(null);
            }

            // 파일 읽기 가능 확인
            if (!pdfFile.canRead()) {
                log.error("❌ PDF 파일을 읽을 수 없습니다 (권한 문제): {}", pdfPath);
                return ResponseEntity.status(403).body(null);
            }

            // 파일 리소스 생성
            Resource resource = new FileSystemResource(pdfFile);
            String filename = "analysis_report_" + bidId + ".pdf";

            log.info("✅ PDF 파일 전송 - 크기: {} bytes", pdfFile.length());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ PDF 다운로드 실패 - Bid ID: {}", bidId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 4. 분석 리포트 (마크다운) 조회 API
     * GET /api/analysis/report/{bidId}
     */
    @GetMapping("/report/{bidId}")
    public ResponseEntity<String> getReport(@PathVariable Integer bidId) {
        try {
            AnalysisResult result = analysisResultRepository.findByBidBidId(bidId)
                    .orElseThrow(() -> new RuntimeException("분석 결과를 찾을 수 없습니다."));

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(result.getAnalysisContent());

        } catch (Exception e) {
            log.error("리포트 조회 실패 - Bid ID: {}", bidId, e);
            return ResponseEntity.status(404).body("리포트 없음: " + e.getMessage());
        }
    }
}
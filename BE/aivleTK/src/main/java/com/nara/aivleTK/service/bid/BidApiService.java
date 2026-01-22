package com.nara.aivleTK.service.bid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.bid.BidApiDto;
import com.nara.aivleTK.dto.bid.BidPriceApiDto; // ★ 추가: 가격정보 DTO
import com.nara.aivleTK.repository.BidRepository;
import com.nara.aivleTK.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidApiService {

    private final BidRepository bidRepository;
    private final AnalysisService analysisService;
    private final String SERVICE_KEY = "c1588436fef59fe2109d0eb3bd03747f61c57a482a6d0052de14f85b0bb02fb2";

    public String fetchAndSaveBidData() {
        try {
            // === 1. [공고 목록 API 호출] ===
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusHours(12);
            LocalDateTime end = now.plusHours(12);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            StringBuilder listUrlBuilder = new StringBuilder("http://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfoCnstwk");
            listUrlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
            listUrlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("200", "UTF-8"));
            listUrlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            listUrlBuilder.append("&" + URLEncoder.encode("inqryDiv", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            listUrlBuilder.append("&" + URLEncoder.encode("inqryBgnDt", "UTF-8") + "=" + URLEncoder.encode(start.format(fmt), "UTF-8"));
            listUrlBuilder.append("&" + URLEncoder.encode("inqryEndDt", "UTF-8") + "=" + URLEncoder.encode(end.format(fmt), "UTF-8"));
            listUrlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));

            URL listUrl = new URI(listUrlBuilder.toString()).toURL();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(listUrl);
            JsonNode itemsNode = rootNode.path("response").path("body").path("items");

            if (itemsNode.isMissingNode() || itemsNode.isEmpty()) return "데이터 없음";

            List<Bid> fetchedBids = new ArrayList<>();
            if (itemsNode.isArray()) {
                for (JsonNode node : itemsNode) {
                    fetchedBids.add(mapper.treeToValue(node, BidApiDto.class).toEntity());
                }
            } else {
                fetchedBids.add(mapper.treeToValue(itemsNode.path("item"), BidApiDto.class).toEntity());
            }

            // === 2. [중복 제거] ===
            List<String> realIdsToCheck = fetchedBids.stream().map(Bid::getBidRealId).collect(Collectors.toList());
            List<Bid> existingBids = bidRepository.findByBidRealIdIn(realIdsToCheck);
            Set<String> existingIds = existingBids.stream().map(Bid::getBidRealId).collect(Collectors.toSet());

            List<Bid> newBidsToSave = fetchedBids.stream()
                    .filter(bid -> !existingIds.contains(bid.getBidRealId()))
                    .collect(Collectors.toList());

            // === 3. [상세 정보 병합 Loop] ===
            for (Bid bid : newBidsToSave) {
                try {
                    // (A) 지역 정보 병합
                    String permittedRegion = getPermittedRegion(bid.getBidRealId());
                    bid.setRegion(permittedRegion);

                    // (B) ★ [수정됨] 기초금액 로직: API 조회 시도 -> 실패 시 계산(1.1배)
                    BidPriceApiDto priceInfo = getBidPriceInfo(bid.getBidRealId());

                    if (priceInfo != null && !priceInfo.getBasicPrice().equals(java.math.BigInteger.ZERO)) {
                        // Case 1: API에 정확한 기초금액이 있는 경우
                        bid.setBasicPrice(priceInfo.getBasicPrice());
                        bid.setBidRange(priceInfo.getBidRangeAbs());
                    } else {
                        // Case 2: API에 데이터가 없는 경우 (404 or 0) -> Fallback: 추정금액 * 1.1
                        if (bid.getEstimatePrice() != null) {
                            // BigInteger -> BigDecimal 변환 후 1.1 곱하기 -> 다시 BigInteger
                            java.math.BigDecimal estPrice = new java.math.BigDecimal(bid.getEstimatePrice());
                            java.math.BigInteger calculatedBasicPrice = estPrice.multiply(java.math.BigDecimal.valueOf(1.1)).toBigInteger();

                            bid.setBasicPrice(calculatedBasicPrice);
                        }
                        // 투찰범위는 기본값(예: 0.0 또는 2.0/3.0 등 정책에 맞게) 설정하거나 비워둠
                    }

                    // 서버 부하 방지용 딜레이
                    Thread.sleep(50);

                } catch (Exception e) {
                    log.error("상세 정보 병합 중 에러 (ID: {}): {}", bid.getBidRealId(), e.getMessage());
                }
            }

            // === 4. [최종 저장 및 AI 분석 요청] ===
            if (!newBidsToSave.isEmpty()) {
                List<Bid> savedBids = bidRepository.saveAll(newBidsToSave);

                int analysisCount = 0;
                for (Bid bid : savedBids) {
                    try {
                        analysisService.analyzeAndSave(bid.getBidId());
                        analysisCount++;
                    } catch (Exception e) {
                        log.error("AI 분석 요청 실패 (ID: {}): {}", bid.getBidId(), e.getMessage());
                    }
                }
                return "신규 " + savedBids.size() + "건 저장 완료, " + analysisCount + "건 분석 요청됨";
            }

            return "신규 데이터 없음";

        } catch (Exception e) {
            log.error("Error", e);
            return "에러: " + e.getMessage();
        }
    }

    // === [Helper 1] 참가가능지역 조회 ===
    private String getPermittedRegion(String fullBidNtceNo) {
        String baseNo = fullBidNtceNo;
        String ord = "00";
        if (fullBidNtceNo.contains("-")) {
            String[] parts = fullBidNtceNo.split("-");
            baseNo = parts[0];
            if (parts.length > 1) ord = parts[1];
        }

        try {
            StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfoPrtcptPsblRgn");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("bidNtceNo", "UTF-8") + "=" + URLEncoder.encode(baseNo, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("bidNtceOrd", "UTF-8") + "=" + URLEncoder.encode(ord, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("inqryDiv", "UTF-8") + "=" + URLEncoder.encode("2", "UTF-8"));
            URL url = new URI(urlBuilder.toString()).toURL();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(url);
            JsonNode itemsNode = rootNode.path("response").path("body").path("items");

            if (itemsNode.isMissingNode() || itemsNode.isEmpty()) return "전국";

            List<String> regions = new ArrayList<>();
            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    if (item.has("prtcptPsblRgnNm")) regions.add(item.get("prtcptPsblRgnNm").asText());
                }
            } else {
                if (itemsNode.has("item")) regions.add(itemsNode.path("item").path("prtcptPsblRgnNm").asText());
            }

            if (regions.isEmpty()) return "전국";
            return String.join(", ", regions);

        } catch (Exception e) {
            return "전국";
        }
    }

    // === [Helper 2] ★ 기초금액/투찰범위 조회 (New!) ===
    private BidPriceApiDto getBidPriceInfo(String fullBidNtceNo) {
        String baseNo = fullBidNtceNo;
        String ord = "00";
        if (fullBidNtceNo.contains("-")) {
            String[] parts = fullBidNtceNo.split("-");
            baseNo = parts[0];
            if (parts.length > 1) ord = parts[1];
        }

        try {
            // URL: getBidPblancListInfoCnstwkBsisAmount (공사 기초금액 조회)
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1230000/ad/BidPublicInfoService/getBidPblancListInfoCnstwkBsisAmount");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("inqryDiv", "UTF-8") + "=" + URLEncoder.encode("2", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("bidNtceNo", "UTF-8") + "=" + URLEncoder.encode(baseNo, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("bidNtceOrd", "UTF-8") + "=" + URLEncoder.encode(ord, "UTF-8"));

            URL url = new URI(urlBuilder.toString()).toURL();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(url);
            JsonNode itemsNode = rootNode.path("response").path("body").path("items");

            if (itemsNode.isMissingNode() || itemsNode.isEmpty()) return null;

            // 아이템이 여러 개일 수도 있지만, 보통 공고당 하나이거나 첫 번째가 메인입니다.
            JsonNode targetNode;
            if (itemsNode.isArray()) {
                targetNode = itemsNode.get(0);
            } else {
                targetNode = itemsNode.path("item");
            }

            // DTO로 변환
            return mapper.treeToValue(targetNode, BidPriceApiDto.class);

        } catch (java.io.FileNotFoundException e) {
            // 404 데이터 없음 -> null 반환하여 위에서 *1.1 계산 로직을 타게 유도
            return null;
        } catch (Exception e) {
            log.warn("기초금액 API 호출 실패 [ID: {}] : {}", fullBidNtceNo, e.toString());
            return null;
        }
    }
    @Transactional // DB 수정을 위해 필수
    public void updateMissingData() {
        log.info("=== [데이터 보정] 누락된 기초금액/투찰범위 재수집 시작 ===");

        // 1. 보정 대상 조회 (마감 안 됐고, 투찰범위가 0.0인 것들)
        List<Bid> incompleteBids = bidRepository.findByEndDateAfterAndBidRange(LocalDateTime.now(), 0.0);

        int updateCount = 0;

        for (Bid bid : incompleteBids) {
            try {
                // 2. API 다시 조회 (기존에 만든 메서드 재사용!)
                BidPriceApiDto priceInfo = getBidPriceInfo(bid.getBidRealId());

                // 3. 데이터가 이제 떴다면? -> Update
                if (priceInfo != null && !priceInfo.getBasicPrice().equals(java.math.BigInteger.ZERO)) {
                    bid.setBasicPrice(priceInfo.getBasicPrice()); // 진짜 기초금액으로 덮어쓰기
                    bid.setBidRange(priceInfo.getBidRangeAbs());  // 투찰범위 저장

                    // (선택사항) 값이 바뀌었으니 AI 분석도 다시 요청할 수 있음
                    // analysisService.analyzeAndSave(bid.getBidId());

                    updateCount++;
                    log.info("데이터 업데이트 성공 (ID: {})", bid.getBidRealId());
                }

                Thread.sleep(50); // API 보호

            } catch (Exception e) {
                log.warn("보정 중 에러 (ID: {}): {}", bid.getBidRealId(), e.getMessage());
            }
        }

        log.info("=== [데이터 보정] 종료. 총 {}건 업데이트 완료 ===", updateCount);
    }
}

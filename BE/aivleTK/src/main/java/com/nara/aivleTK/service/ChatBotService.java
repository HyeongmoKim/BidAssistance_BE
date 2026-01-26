//package com.nara.aivleTK.service;
//
//import com.nara.aivleTK.domain.Bid;
//import com.nara.aivleTK.dto.chatBot.AiIntentResponse;
//import com.nara.aivleTK.dto.chatBot.ChatResponse;
//import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
//import com.nara.aivleTK.repository.BidRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatBotService {
//
//    private final BidRepository bidRepository;
//    private final RestTemplate restTemplate;
//
//    // 파이썬 서버 주소 (FastAPI 기준)
//    private final String PYTHON_URL = "http://localhost:5000/py-api";
//
//    public ChatResponse getChatResponse(String prompt) {
//        //챗봇 검색 키워드 생성 api주소
//        String intentURL = PYTHON_URL + "/chatbot-intent";
//        PythonChatRequest intentRequest = new PythonChatRequest();
//        AiIntentResponse intent = null;
//        try {
//            intent = restTemplate.postForObject(intentURL, intentRequest, AiIntentResponse.class);
//            log.info("AI 의도 분석 결과 : {}", intent);
//        } catch (Exception e) {
//            log.error("AI의도 분석 실패 : {}", e.getMessage());
//            return new ChatResponse("AI서버 연결 원활하지 않음");
//        }
//        List<Bid> searchResults = new ArrayList<>();
//        if (intent != null && "search".equals(intent.getType())) {
//            searchResults = bidRepository.searchDetail(
//                    intent.getKeyword(),
//                    intent.getRegion(),
//                    intent.getAgency(),   // 추가됨 (없으면 null)
//                    intent.getMinPrice(), // 추가됨 (없으면 null)
//                    intent.getMaxPrice()  // 추가됨 (없으면 null)
//            );
//            if (searchResults.size() > 5) {
//                searchResults = searchResults.subList(0, 5);
//            }
//        }
//        //챗봇 결과 생성 api주소
//        String generateUrl = PYTHON_URL + "/chatbot-answer";
//        List<Map<String, Object>> contextData = convertBidsToMap(searchResults);
//        PythonChatRequest answerRequest = new PythonChatRequest(prompt, contextData);
//        try {
//            ChatResponse answer = restTemplate.postForObject(generateUrl, answerRequest, ChatResponse.class);
//            return answer;
//        } catch (Exception e) {
//            log.error("AI 답변 생성 실패 {}", e.getMessage());
//            return new ChatResponse("답변 생성 중 오류가 발생했습니다.");
//        }
//    }
//
//        private List<Map<String, Object>> convertBidsToMap(List<Bid> bids) {
//            List<Map<String, Object>> result = new ArrayList<>();
//            for (Bid bid : bids) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("공고명", bid.getName());
//                map.put("지역", bid.getRegion());
//                map.put("수요기관", bid.getOrganization());
//                map.put("기초금액", bid.getBasicPrice());
//                map.put("링크", bid.getBidURL());
//                result.add(map);
//            }
//            return result;
//    }
//}

package com.nara.aivleTK.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.chatBot.ChatResponse;
import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
import com.nara.aivleTK.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private final BidRepository bidRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 추가

    // 파이썬 서버 주소 (main.py가 8000포트에서 실행됨)
    private final String PYTHON_URL = "http://localhost:8000/chat";

    public ChatResponse getChatResponse(String prompt) {
        // 1. 파이썬 LangGraph 에이전트 호출
        PythonChatRequest request = new PythonChatRequest(prompt, "user_session_1"); // 세션 ID는 필요시 동적으로 변경

        try {
            // 파이썬 서버로 요청 전송
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(PYTHON_URL, request, Map.class);
            Map<String, Object> body = responseEntity.getBody();

            if (body == null || !body.containsKey("response")) {
                return new ChatResponse("AI 서버로부터 응답을 받지 못했습니다.");
            }

            String aiResponse = (String) body.get("response");

            // 2. 응답 내용 분석 (JSON인지 일반 텍스트인지 판단)
            if (isSearchIntent(aiResponse)) {
                // [검색 의도] 파이썬이 JSON 필터를 반환한 경우 -> DB 조회 수행
                return handleSearchIntent(aiResponse);
            } else {
                // [일반 대화/사용법] 파이썬이 텍스트 답변을 준 경우 -> 그대로 반환
                return new ChatResponse(aiResponse);
            }

        } catch (Exception e) {
            log.error("AI 서버 연결 실패 : {}", e.getMessage());
            return new ChatResponse("AI 서버 연결이 원활하지 않습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    // 파이썬 응답이 JSON 형식(검색 필터)인지 확인
    private boolean isSearchIntent(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            // JSON이고 "intent" 필드가 있으면 검색 명령으로 간주
            return node.has("intent") && node.has("filter");
        } catch (Exception e) {
            return false; // JSON 파싱 실패 시 일반 텍스트로 간주
        }
    }

    // DB 검색 수행 및 결과 포맷팅
    private ChatResponse handleSearchIntent(String jsonString) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            JsonNode filter = root.path("filter");

            // 1. 파이썬의 복잡한 JSON 필터를 자바 변수로 매핑
            String region = filter.path("region").isNull() ? null : filter.path("region").asText();
            String agency = filter.path("organization").isNull() ? null : filter.path("organization").asText();

            // 공고명(Keyword)은 현재 파이썬 필터에 없으므로 null 처리하거나, 필요시 파이썬 툴 수정 필요
            // 여기서는 지역/기관/가격 조건만으로 검색
            String keyword = null;

            // 가격 범위 파싱 (파이썬: estimatePrice.from.value / to.value)
            Long minPrice = null;
            Long maxPrice = null;

            JsonNode basicPriceNode = filter.path("basicPrice"); // 기초금액 기준
            if (!basicPriceNode.isMissingNode() && !basicPriceNode.isNull()) {
                if (basicPriceNode.has("from") && !basicPriceNode.path("from").isNull()) {
                    minPrice = basicPriceNode.path("from").path("value").asLong();
                }
                if (basicPriceNode.has("to") && !basicPriceNode.path("to").isNull()) {
                    maxPrice = basicPriceNode.path("to").path("value").asLong();
                }
            }

            log.info("DB 검색 실행 - 지역: {}, 기관: {}, 최소금액: {}, 최대금액: {}", region, agency, minPrice, maxPrice);

            // 2. 리포지토리 조회
            List<Bid> searchResults = bidRepository.searchDetail(
                    keyword,
                    region,
                    agency,
                    minPrice,
                    maxPrice
            );

            // 3. 결과를 텍스트(문자열)로 변환하여 반환
            if (!searchResults.isEmpty()) {
                return new ChatResponse(
                        "검색 결과 " + searchResults.size() + "건을 찾았습니다.", // 텍스트 메시지
                        "list",                                              // UI 타입
                        searchResults                                        // ★ 실제 데이터 리스트
                );
            }

            return new ChatResponse(formatBidsToString(searchResults));

            // ChatBotService.java의 아래쪽 catch 블록 수정

        } catch (Exception e) {
            // 로그에도 남기고
            log.error("상세 에러 로그: ", e);

            // ★ 채팅창에 에러 원인을 그대로 출력 (범인 검거용)
            return new ChatResponse("🚨 에러 발생: " + e.getMessage());
        }
    }

    // 공고 리스트를 사용자가 보기 좋은 문자열로 변환
    private String formatBidsToString(List<Bid> bids) {
        StringBuilder sb = new StringBuilder();
        sb.append("총 ").append(bids.size()).append("건의 공고가 검색되었습니다.\n\n");

        int count = 0;
        for (Bid bid : bids) {
            if (count >= 5) break; // 최대 5개만 표시
            sb.append(count + 1).append(". ").append(bid.getName()).append("\n");
            sb.append("   - 지역: ").append(bid.getRegion() != null ? bid.getRegion() : "전국").append("\n");
            sb.append("   - 기관: ").append(bid.getOrganization()).append("\n");
            sb.append("   - 금액: ").append(String.format("%,d", bid.getBasicPrice())).append("원\n");
            sb.append("   - 링크: ").append(bid.getBidURL()).append("\n\n");
            count++;
        }

        if (bids.size() > 5) {
            sb.append("...외 ").append(bids.size() - 5).append("건이 더 있습니다.");
        }

        return sb.toString();
    }
}
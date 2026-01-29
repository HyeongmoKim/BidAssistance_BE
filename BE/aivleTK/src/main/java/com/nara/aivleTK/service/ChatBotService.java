
package com.nara.aivleTK.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.chatBot.ChatResponse;
import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
import com.nara.aivleTK.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private final BidRepository bidRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ★ 1. 파이썬 서버 주소 (ngrok 주소 또는 로컬 주소 확인)
    private final String PYTHON_URL = "https://aivleachatbot.greenpond-9eab36ab.koreacentral.azurecontainerapps.io/chat";

    public ChatResponse getChatResponse(String prompt) {
        PythonChatRequest requestPayload = new PythonChatRequest(prompt, "user_session_1");

        // ngrok 헤더 처리
        HttpHeaders headers = new HttpHeaders();
        headers.add("ngrok-skip-browser-warning", "true");
        headers.add("Content-Type", "application/json");
        HttpEntity<PythonChatRequest> entity = new HttpEntity<>(requestPayload, headers);



        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(PYTHON_URL, entity, Map.class);
            Map<String, Object> body = responseEntity.getBody();

            // 1. 응답 null 체크
            if (body == null || !body.containsKey("response")) {
                return new ChatResponse("AI 서버로부터 응답이 없습니다.");
            }

            // 2. 응답 내용 추출
            String aiContent = (String) body.get("response");
            log.info("AI 응답 데이터: {}", aiContent);

            // 3. 의도(Intent) 파악 및 분기 처리
            if (isSearchIntent(aiContent)) {
                return handleSearchIntent(aiContent); // DB 조회 로직 실행
            } else {
                return new ChatResponse(aiContent);   // 일반 대화 반환
            }

        } catch (Exception e) {
            log.error("AI 서버 연결 실패: {}", e.getMessage());
            return new ChatResponse("시스템 에러: " + e.getMessage());
        }
    }

    private boolean isSearchIntent(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            return node.has("intent") && node.has("filter");
        } catch (Exception e) {
            return false;
        }
    }

    private ChatResponse handleSearchIntent(String jsonString) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            JsonNode filter = root.path("filter");

            // --- 1. 기본 필드 파싱 ---
            String bidRealId = filter.path("bidRealId").isNull() ? null : filter.path("bidRealId").asText();
            String region = filter.path("region").isNull() ? null : filter.path("region").asText();
            String organization = filter.path("organization").isNull() ? null : filter.path("organization").asText();
            // 파이썬 툴에는 'keyword'가 없지만 필요 시 추가 가능. 현재는 null 처리
            String keyword = null;

            // --- 2. 금액/비율 범위 파싱 (Helper 메서드 사용) ---
            Long minBasicPrice = parseLongValue(filter.path("basicPrice"), "from");
            Long maxBasicPrice = parseLongValue(filter.path("basicPrice"), "to");

            Long minEstimatePrice = parseLongValue(filter.path("estimatePrice"), "from");
            Long maxEstimatePrice = parseLongValue(filter.path("estimatePrice"), "to");

            Double minBidRate = parseDoubleValue(filter.path("minimumBidRate"), "from");
            Double maxBidRate = parseDoubleValue(filter.path("minimumBidRate"), "to");

            Double minBidRange = parseDoubleValue(filter.path("bidRange"), "from");
            Double maxBidRange = parseDoubleValue(filter.path("bidRange"), "to");

            // --- 3. 날짜 조건 파싱 ---
            LocalDateTime startDateFrom = null; LocalDateTime startDateTo = null;
            LocalDateTime endDateFrom = null; LocalDateTime endDateTo = null;
            LocalDateTime openDateFrom = null; LocalDateTime openDateTo = null;

            JsonNode timeRange = filter.path("timeRange");
            if (!timeRange.isMissingNode() && !timeRange.isNull()) {
                String base = timeRange.path("base").asText(); // startDate, endDate, openDate

                LocalDateTime fromDate = parseDateValue(timeRange.path("from"));
                LocalDateTime toDate = parseDateValue(timeRange.path("to"));

                if ("startDate".equals(base)) {
                    startDateFrom = fromDate; startDateTo = toDate;
                } else if ("endDate".equals(base)) {
                    endDateFrom = fromDate; endDateTo = toDate;
                } else if ("openDate".equals(base)) {
                    openDateFrom = fromDate; openDateTo = toDate;
                }
            }

            // --- 4. DB 조회 실행 (파라미터 순서 정확해야 함) ---
            List<Bid> searchResults = bidRepository.searchDetail(
                    bidRealId, keyword, region, organization,
                    minBasicPrice, maxBasicPrice,
                    minEstimatePrice, maxEstimatePrice,
                    minBidRate, maxBidRate,
                    minBidRange, maxBidRange,
                    startDateFrom, startDateTo,
                    endDateFrom, endDateTo,
                    openDateFrom, openDateTo,
                    LocalDateTime.now()
            );

            if (!searchResults.isEmpty()) {
                return new ChatResponse("검색 결과 " + searchResults.size() + "건을 찾았습니다.", "list", searchResults);
            } else {
                return new ChatResponse("조건에 맞는 공고가 없습니다.");
            }

        } catch (Exception e) {
            log.error("검색 처리 중 오류", e);
            return new ChatResponse("검색 조건을 처리하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // --- Helper Methods ---

    // JSON { "from": { "value": 100 } } 에서 값 추출
    private Long parseLongValue(JsonNode parentNode, String direction) {
        if (parentNode.isMissingNode() || parentNode.isNull()) return null;
        JsonNode target = parentNode.path(direction);
        if (target.has("value") && !target.path("value").isNull()) {
            return target.path("value").asLong();
        }
        return null;
    }

    private Double parseDoubleValue(JsonNode parentNode, String direction) {
        if (parentNode.isMissingNode() || parentNode.isNull()) return null;
        JsonNode target = parentNode.path(direction);
        if (target.has("value") && !target.path("value").isNull()) {
            return target.path("value").asDouble();
        }
        return null;
    }

    // yyyyMMddHHmm 형식 숫자를 LocalDateTime으로 변환
    private LocalDateTime parseDateValue(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;

        String kind = node.path("kind").asText();

        // 1. 절대 날짜 (Absolute) 처리
        if ("absolute".equals(kind)) {
            long val = node.path("value").asLong();
            if (val == 0) return null;
            try {
                String dateStr = String.valueOf(val);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                log.warn("절대 날짜 파싱 실패: " + val);
                return null;
            }
        }

        // 2. 상대 날짜 (Calendar) 처리 ★핵심 구현★
        else if ("calendar".equals(kind)) {
            String unit = node.path("unit").asText();     // day, week, month, year
            int offset = node.path("offset").asInt();     // 0, 1, -1 ...
            String position = node.path("position").asText(); // start, end

            return calculateCalendarDate(unit, offset, position);
        }

        return null;
    }
    private LocalDateTime calculateCalendarDate(String unit, int offset, String position) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDate = now;

        // 1. 단위(unit)에 따른 이동
        switch (unit) {
            case "day":
                targetDate = now.plusDays(offset);
                break;
            case "week":
                targetDate = now.plusWeeks(offset);
                break;
            case "month":
                targetDate = now.plusMonths(offset);
                break;
            case "year":
                targetDate = now.plusYears(offset);
                break;
            default:
                return now;
        }

        // 2. 위치(position)에 따른 시점 조정 (시작일/종료일)
        if ("start".equals(position)) {
            switch (unit) {
                case "week": // 해당 주의 월요일 00:00
                    targetDate = targetDate.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "month": // 해당 월의 1일 00:00
                    targetDate = targetDate.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "year": // 해당 연도의 1월 1일 00:00
                    targetDate = targetDate.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "day": // 해당 일의 00:00
                    targetDate = targetDate.withHour(0).withMinute(0).withSecond(0);
                    break;
            }
        } else if ("end".equals(position)) {
            switch (unit) {
                case "week": // 해당 주의 일요일 23:59
                    targetDate = targetDate.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "month": // 해당 월의 마지막 날 23:59
                    targetDate = targetDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "year": // 해당 연도의 12월 31일 23:59
                    targetDate = targetDate.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "day": // 해당 일의 23:59
                    targetDate = targetDate.withHour(23).withMinute(59).withSecond(59);
                    break;
            }
        }

        return targetDate;
    }

}

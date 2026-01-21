package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.chatBot.ChatResponse;
import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
import com.nara.aivleTK.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final BidRepository bidRepository;
    private final RestTemplate restTemplate;

    // 파이썬 서버 주소 (FastAPI 기준)
    private final String PYTHON_URL = "http://localhost:5000/py-api/analyze";

    public ChatResponse getAiAnalysis(String userPrompt) {
        // 1. DB에서 Bid 테이블 전체 데이터 가져오기
        List<Bid> allBids = bidRepository.findAll();

        // 2. [중요] 엔티티 -> Map 변환 (순환참조 에러 방지 및 데이터 정제)
        List<Map<String, Object>> cleanData = new ArrayList<>();

        for (Bid bid : allBids) {
            Map<String, Object> map = new HashMap<>();
            // 필요한 필드만 직접 넣으세요. 에러 절대 안 납니다.
            map.put("bidId", bid.getBidId());
            map.put("name", bid.getName());
            map.put("organization", bid.getOrganization());
            map.put("estimatePrice", bid.getEstimatePrice());
            map.put("region", bid.getRegion());
            // 날짜 타입 처리 (Null 체크 포함)
            map.put("startDate", bid.getStartDate() != null ? bid.getStartDate().toString() : "");
            map.put("endDate", bid.getEndDate() != null ? bid.getEndDate().toString() : "");

            cleanData.add(map);
        }

        // 3. 파이썬으로 보낼 객체 생성 (질문 + 데이터)
        PythonChatRequest requestToPython = new PythonChatRequest(userPrompt, cleanData);

        // 4. 전송 및 결과 수신
        try {
            // postForObject(주소, 보낼데이터, 받을타입)
            ChatResponse response = restTemplate.postForObject(PYTHON_URL, requestToPython, ChatResponse.class);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("파이썬 서버 연결 실패: " + e.getMessage());
        }
    }
}
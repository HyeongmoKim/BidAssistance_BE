package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.dto.chatBot.AiIntentResponse;
import com.nara.aivleTK.dto.chatBot.ChatResponse;
import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
import com.nara.aivleTK.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private final BidRepository bidRepository;
    private final RestTemplate restTemplate;

    // 파이썬 서버 주소 (FastAPI 기준)
    private final String PYTHON_URL = "http://localhost:5000/py-api";

    public ChatResponse getChatResponse(String prompt) {
        //챗봇 검색 키워드 생성 api주소
        String intentURL = PYTHON_URL + "/chatbot-intent";
        PythonChatRequest intentRequest = new PythonChatRequest();
        AiIntentResponse intent = null;
        try {
            intent = restTemplate.postForObject(intentURL, intentRequest, AiIntentResponse.class);
            log.info("AI 의도 분석 결과 : {}", intent);
        } catch (Exception e) {
            log.error("AI의도 분석 실패 : {}", e.getMessage());
            return new ChatResponse("AI서버 연결 원활하지 않음");
        }
        List<Bid> searchResults = new ArrayList<>();
        if (intent != null && "search".equals(intent.getType())) {
            searchResults = bidRepository.searchDetail(intent.getKeyword(), intent.getRegion());
            if (searchResults.size() > 5) {
                searchResults = searchResults.subList(0, 5);
            }
        }
        //챗봇 결과 생성 api주소
        String generateUrl = PYTHON_URL + "/chatbot-answer";
        List<Map<String, Object>> contextData = convertBidsToMap(searchResults);
        PythonChatRequest answerRequest = new PythonChatRequest(prompt, contextData);
        try {
            ChatResponse answer = restTemplate.postForObject(generateUrl, answerRequest, ChatResponse.class);
            return answer;
        } catch (Exception e) {
            log.error("AI 답변 생성 실패 {}", e.getMessage());
            return new ChatResponse("답변 생성 중 오류가 발생했습니다.");
        }
    }

        private List<Map<String, Object>> convertBidsToMap(List<Bid> bids) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Bid bid : bids) {
                Map<String, Object> map = new HashMap<>();
                map.put("공고명", bid.getName());
                map.put("지역", bid.getRegion());
                map.put("수요기관", bid.getOrganization());
                map.put("기초금액", bid.getBasicPrice());
                map.put("링크", bid.getBidURL());
                result.add(map);
            }
            return result;
    }
}
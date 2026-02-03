package com.nara.aivleTK.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Qualifier;


import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class ChatbotClient {
    private final RestClient restClient;

    public ChatbotClient(@Qualifier("pythonRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public String sendFileAndText(String text, MultipartFile file){
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("text",text);
            if(file!=null&&!file.isEmpty()){
                Resource fileResource = convertToFileResource(file);
                body.add("file",file);
            }
            Map response = restClient.post()
                    .uri("/chat/file")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if(response!=null&& response.containsKey("response")){
                return String.valueOf(response.get("response"));
            }
            return "AI서버 응답없음";

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private Resource convertToFileResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                // 파일명이 누락되지 않도록 원본 파일명 반환
                return file.getOriginalFilename();
            }
        };
    }
}

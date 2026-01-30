package com.nara.aivleTK.controller;

import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.dto.chatBot.ChatResponse;
import com.nara.aivleTK.dto.chatBot.PythonChatRequest;
import com.nara.aivleTK.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbots")
@RequiredArgsConstructor
public class ChatBotController {
    private final ChatBotService chatBotService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody PythonChatRequest request){
        ChatResponse response = chatBotService.getChatResponse(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.nara.aivleTK.controller;

import com.nara.aivleTK.domain.Alarm;
import com.nara.aivleTK.dto.ApiResponse;
import com.nara.aivleTK.service.bid.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    // 1. 알림 생성 API (보통 시스템 로직에서 호출되지만 테스트용으로 오픈)
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createAlarm(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer bidId,
            @RequestParam String content) {
        alarmService.createAlarm(userId, bidId, content);
        return ResponseEntity.ok(ApiResponse.success("알림이 생성되었습니다."));
    }

    // 2. 내 알림 목록 조회 API
    @GetMapping("/{userId:\\d+}")
    public ResponseEntity<ApiResponse<List<Alarm>>> getMyAlarms(@PathVariable Integer userId) {
        List<Alarm> alarms = alarmService.getMyAlarms(userId);
        return ResponseEntity.ok(ApiResponse.success(alarms));
    }

    // 3. 알림 삭제 API
    @DeleteMapping("/{alarmId:\\d+}")
    public ResponseEntity<ApiResponse<Object>> deleteAlarm(@PathVariable Integer alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.ok(ApiResponse.success("알림이 삭제되었습니다."));
    }

    // 4. 이메일 알림 발송 API
    @PostMapping("/email")
    public ResponseEntity<String> sendEmailNotification(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        try {
            alarmService.sendEmailNotification(email, subject, content);
            return ResponseEntity.ok("이메일 알림이 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 발송 실패: " + e.getMessage());
        }
    }
}
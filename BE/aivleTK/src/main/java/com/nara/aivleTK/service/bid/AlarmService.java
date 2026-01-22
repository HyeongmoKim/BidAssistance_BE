package com.nara.aivleTK.service.bid;

import com.nara.aivleTK.domain.Alarm;
import java.util.List;

public interface AlarmService {
    // 알림 생성 로직
    void createAlarm(Integer userId, Integer bidId, String content);

    // 사용자의 알림 목록 조회 로직
    List<Alarm> getMyAlarms(Integer userId);

    // 특정 알림 삭제 로직
    void deleteAlarm(Integer alarmId);
    // 이메일 알림 발송
    void sendEmailNotification(String email, String subject, String content);
}
package com.nara.aivleTK.service.bid;

import com.nara.aivleTK.domain.Alarm;
import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.domain.user.User;
import com.nara.aivleTK.exception.ResourceNotFoundException;
import com.nara.aivleTK.repository.AlarmRepository;
import com.nara.aivleTK.repository.BidRepository;
import com.nara.aivleTK.repository.UserRepository;
import com.nara.aivleTK.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public void createAlarm(Integer userId, Integer bidId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // bidId가 있을 경우에만 Bid 엔티티 조회 (null 허용)
        Bid bid = (bidId != null) ? bidRepository.findById(bidId).orElse(null) : null;

        Alarm alarm = Alarm.builder()
                .user(user)
                .bid(bid)
                .content(content)
                .date(LocalDateTime.now()) // 알림 발생 시각 기록
                .build();
        alarmRepository.save(alarm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getMyAlarms(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // 최신순 조회
        return alarmRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    @Transactional
    public void deleteAlarm(Integer alarmId) {
        alarmRepository.deleteById(alarmId);
    }
    @Override
    public void sendEmailNotification(String email, String subject, String content) {
        mailService.sendAlarmNotification(email, subject, content);
    }
}
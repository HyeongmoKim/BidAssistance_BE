package com.nara.aivleTK.service.bid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidScheduler {
    private final BidApiService bidApiService;

    //@Scheduled(cron = "0 * * * * *") // 추후 갱신 간격 정해야함 현재 매일 1시
    public void autoFetch(){
        log.info("스케쥴러 실행");
        try{
            String result = bidApiService.fetchAndSaveBidData();
            log.info("스케쥴러 종료 결과 : {}",result);
        } catch (Exception e){
            log.error("오류 발생",e);
        }
    }
}

package com.nara.aivleTK.service.bid;

import com.nara.aivleTK.domain.BidLog;
import java.math.BigInteger;
import java.util.List;

public interface BidLogService {
    void recordBid(Integer userId, Integer bidId, BigInteger price); // 입찰 기록 저장
    List<BidLog> getUserBidLogs(Integer userId); // 유저별 입찰 내역 조회
}
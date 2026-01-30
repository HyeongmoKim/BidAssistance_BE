package com.nara.aivleTK.repository;

import com.nara.aivleTK.domain.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    boolean existsByBidRealId(String realId);

    List<Bid> findByNameContainingOrOrganizationContainingOrRegionContaining(String name, String organization, String region);

    /**
     * Python 검색 툴 필터 적용 + 마감 임박순 정렬 쿼리
     */
    @Query("SELECT b FROM Bid b WHERE " +
            "(:bidRealId IS NULL OR b.bidRealId = :bidRealId) " +
            "AND (:keyword IS NULL OR b.name LIKE %:keyword%) " +
            "AND (:region IS NULL OR b.region LIKE %:region%) " +
            "AND (:organization IS NULL OR b.organization LIKE %:organization%) " +
            "AND (:minBasicPrice IS NULL OR b.basicPrice >= :minBasicPrice) " +
            "AND (:maxBasicPrice IS NULL OR b.basicPrice <= :maxBasicPrice) " +
            "AND (:minEstimatePrice IS NULL OR b.estimatePrice >= :minEstimatePrice) " +
            "AND (:maxEstimatePrice IS NULL OR b.estimatePrice <= :maxEstimatePrice) " +
            "AND (:minBidRate IS NULL OR b.minimumBidRate >= :minBidRate) " +
            "AND (:maxBidRate IS NULL OR b.minimumBidRate <= :maxBidRate) " +
            "AND (:minBidRange IS NULL OR b.bidRange >= :minBidRange) " +
            "AND (:maxBidRange IS NULL OR b.bidRange <= :maxBidRange) " +
            "AND (:startDateFrom IS NULL OR b.startDate >= :startDateFrom) " +
            "AND (:startDateTo IS NULL OR b.startDate <= :startDateTo) " +
            "AND (:endDateFrom IS NULL OR b.endDate >= :endDateFrom) " +
            "AND (:endDateTo IS NULL OR b.endDate <= :endDateTo) " +
            "AND (:openDateFrom IS NULL OR b.openDate >= :openDateFrom) " +
            "AND (:openDateTo IS NULL OR b.openDate <= :openDateTo) " +
            "AND (b.endDate > :now) ")
    Page<Bid> searchDetail(
            @Param("bidRealId") String bidRealId,
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("organization") String organization,
            @Param("minBasicPrice") Long minPrice,
            @Param("maxBasicPrice") Long maxPrice,
            @Param("minEstimatePrice") Long minEstimatePrice,
            @Param("maxEstimatePrice") Long maxEstimatePrice,
            @Param("minBidRate") Double minBidRate,
            @Param("maxBidRate") Double maxBidRate,
            @Param("minBidRange") Double minBidRange,
            @Param("maxBidRange") Double maxBidRange,
            @Param("startDateFrom") LocalDateTime startDateFrom,
            @Param("startDateTo") LocalDateTime startDateTo,
            @Param("endDateFrom") LocalDateTime endDateFrom,
            @Param("endDateTo") LocalDateTime endDateTo,
            @Param("openDateFrom") LocalDateTime openDateFrom,
            @Param("openDateTo") LocalDateTime openDateTo,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<Bid> findByBidRealIdIn(List<String> realIds);
    List<Bid> findTop200ByRegionIsNull();
    List<Bid> findByEndDateAfterAndBidRange(LocalDateTime now, Double bidRange);
    List<Bid> findByEndDateAfter(LocalDateTime now);
}
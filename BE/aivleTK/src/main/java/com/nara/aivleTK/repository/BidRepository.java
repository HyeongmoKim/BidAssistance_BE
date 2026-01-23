package com.nara.aivleTK.repository;

import com.nara.aivleTK.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid,Integer> {
    boolean existsByBidRealId(String realId);
    List<Bid> findByNameContainingOrOrganizationContainingOrRegionContaining(String name, String organization,String region);
    @Query("SELECT b FROM Bid b WHERE " +
            "(:keyword IS NULL OR b.name LIKE %:keyword%) " +
            "AND (:region IS NULL OR b.region LIKE %:region%) " +
            "AND (:agency IS NULL OR b.organization LIKE %:agency%) " + // 기관 검색 추가
            "AND (:minPrice IS NULL OR b.basicPrice >= :minPrice) " +    // 최소 금액 이상
            "AND (:maxPrice IS NULL OR b.basicPrice <= :maxPrice)")      // 최대 금액 이하
    List<Bid> searchDetail(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("agency") String agency,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice
    );
    List<Bid> findByBidRealIdIn(List<String> realIds);
    List<Bid> findTop200ByRegionIsNull();
    List<Bid> findByEndDateAfterAndBidRange(LocalDateTime now, Double bidRange);
    List<Bid> findByEndDateAfter(LocalDateTime now);

}

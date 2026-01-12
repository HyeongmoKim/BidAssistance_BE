package com.nara.aivleTK.repository;

import com.nara.aivleTK.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid,Integer> {
    boolean existsByBidRealId(String realId);
    List<Bid> findByNameContainingOrOrganizationContainingOrRegionContaining(String name, String organization,String region);
    @Query("SELECT b FROM Bid b WHERE " +
            "(:name IS NULL OR b.name LIKE %:name%) AND " +
            "(:region IS NULL OR b.region LIKE %:region%)")
    List<Bid> searchDetail(@Param("name") String name, @Param("region") String region);
    List<Bid> findByBidRealIdIn(List<String> realIds);
    List<Bid> findTop200ByRegionIsNull();

}

package com.nara.aivleTK.repository;


import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBid(Bid bid);
    List<Comment> findByBidBidIdOrderByCommentCreateAtAsc(Integer bidId);
}

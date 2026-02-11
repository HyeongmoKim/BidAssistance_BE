package com.nara.aivleTK.repository;

import com.nara.aivleTK.domain.Comment;
import com.nara.aivleTK.domain.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBoard(Board board);

    // Board ID로 댓글 찾기
    List<Comment> findAllByBoard_Id(int boardId);
}

package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.domain.Comment;
import com.nara.aivleTK.domain.board.Board;
import com.nara.aivleTK.domain.user.User;
import com.nara.aivleTK.dto.comment.CommentCreateRequest;
import com.nara.aivleTK.dto.comment.CommentResponse;
import com.nara.aivleTK.repository.BidRepository;
import com.nara.aivleTK.repository.BoardRepository;
import com.nara.aivleTK.repository.CommentRepository;
import com.nara.aivleTK.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Integer bidId, Integer boardId, CommentCreateRequest request) {
        // 1. 내용 유효성 검사
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }

        // 2. 유저 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found"));

        // 3. 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));
        }

        // 4. 댓글 엔티티 빌더 생성 (공통 필드 설정)
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .commentContent(request.getContent())
                .commentCreateAt(LocalDateTime.now())
                .user(user)
                .parent(parent);

        // 5. Bid 또는 Board 설정 (분기 처리)
        if (bidId != null) {
            Bid bid = bidRepository.findById(bidId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bid not found"));
            commentBuilder.bid(bid);
        } else if (boardId != null) {
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
            commentBuilder.board(board);
        } else {
            // 둘 다 null인 경우 에러 처리
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시글 정보(Bid 또는 Board)가 없습니다.");
        }

        // 6. 저장 및 응답 반환
        Comment comment = commentBuilder.build();
        commentRepository.save(comment);
        return new CommentResponse(comment);
    }


    @Override
    @Transactional
    public void deleteComment(int commentId,int userId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Comment not found"));

        if(!comment.getUser().getId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"삭제할 수 없습니다.");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBid(int bidId){
        return commentRepository.findAllByBid_BidId(bidId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBoard(int boardId){
        return commentRepository.findAllByBoard_Id(boardId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}

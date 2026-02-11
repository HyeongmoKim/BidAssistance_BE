package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.Comment;
import com.nara.aivleTK.domain.board.Board;
import com.nara.aivleTK.domain.user.User;
import com.nara.aivleTK.dto.comment.CommentCreateRequest;
import com.nara.aivleTK.dto.comment.CommentResponse;
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
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Integer boardId, CommentCreateRequest request) {
        // 1. 내용 유효성 검사
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }

        // 2. 유저 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found"));

        // 3. Board 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // 4. 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .commentContent(request.getContent())
                .commentCreateAt(LocalDateTime.now())
                .user(user)
                .board(board)
                // [DB 호환성 패치] 좀비 컬럼 호환성 보장
                .backupContent(request.getContent())
                .backupUserId(user.getId())
                .build();

        commentRepository.save(comment);
        return new CommentResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(int commentId, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제할 수 없습니다.");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBoard(int boardId) {
        return commentRepository.findAllByBoard_Id(boardId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse adoptComment(int commentId, int userId) {
        // 1. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        // 2. 게시글 확인 (Board에 달린 댓글만 채택 가능)
        Board board = comment.getBoard();
        if (board == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시판 댓글만 채택할 수 있습니다.");
        }

        // 3. 질문 카테고리 확인
        if (!"question".equalsIgnoreCase(board.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "질문 카테고리에서만 채택할 수 있습니다.");
        }

        // 4. 질문 작성자 확인
        if (!board.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "질문 작성자만 채택할 수 있습니다.");
        }

        // 5. 자기 댓글 채택 불가
        if (comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인의 댓글은 채택할 수 없습니다.");
        }

        // 6. 이미 채택된 답변이 있는지 확인 (is_adopted = true인 댓글 조회)
        boolean alreadyAdopted = commentRepository.findAllByBoard_Id(board.getId())
                .stream()
                .anyMatch(Comment::getIsAdopted);

        if (alreadyAdopted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 채택된 답변이 있습니다.");
        }

        // 7. 채택 처리
        comment.setIsAdopted(true);

        // 8. 채택된 사용자에게 포인트 부여 (+20)
        User commentAuthor = comment.getUser();
        commentAuthor.addExpertPoints(20);
        userRepository.save(commentAuthor);

        commentRepository.save(comment);

        return new CommentResponse(comment);
    }
}

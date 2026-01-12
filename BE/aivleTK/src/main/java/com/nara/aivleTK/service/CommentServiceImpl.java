package com.nara.aivleTK.service;

import com.nara.aivleTK.domain.Bid;
import com.nara.aivleTK.domain.Comment;
import com.nara.aivleTK.domain.user.User;
import com.nara.aivleTK.dto.comment.CommentCreateRequest;
import com.nara.aivleTK.dto.comment.CommentResponse;
import com.nara.aivleTK.repository.BidRepository;
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
    private  final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse createComment(int bidId, CommentCreateRequest request){
        if(request.getContent()==null || request.getContent().trim().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"댓글 내용을 입력해주세요.");
        }
        Bid bid =bidRepository.findById(bidId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Bid not found"));
        User user=userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not Found"));

        Comment comment = Comment.builder()
                .commentContent(request.getContent())
                .commentCreateAt(LocalDateTime.now())
                .bid(bid)
                .user(user)
                .build();
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
        return commentRepository.findById(bidId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}

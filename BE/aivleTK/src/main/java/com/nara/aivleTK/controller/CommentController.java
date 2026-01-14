package com.nara.aivleTK.controller;

import com.nara.aivleTK.dto.comment.CommentCreateRequest;
import com.nara.aivleTK.dto.comment.CommentResponse;
import com.nara.aivleTK.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bids/{bidId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("bidId") int bidId,
            @RequestBody CommentCreateRequest request
            ){
        CommentResponse response = commentService.createComment(bidId,request);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByBook(
            @PathVariable("bidId") int bidId
    ){
        List<CommentResponse> comments = commentService.getCommentsByBid(bidId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable int commentId,
            @RequestParam("userId") int userId
    ){
        commentService.deleteComment(commentId,userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}

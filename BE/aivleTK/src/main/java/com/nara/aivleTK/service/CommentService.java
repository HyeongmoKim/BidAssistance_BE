package com.nara.aivleTK.service;

import com.nara.aivleTK.dto.comment.CommentCreateRequest;
import com.nara.aivleTK.dto.comment.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(int bidId, CommentCreateRequest request);
    void deleteComment(int commentId, int userId);
    List<CommentResponse> getCommentsByBid(int bidId);
}

package com.nara.aivleTK.dto.comment;

import com.nara.aivleTK.domain.Comment;

import java.time.LocalDateTime;

public class CommentResponse {
    private int commentId;
    private String content;
    private LocalDateTime commentCreatedAt;
    private Integer userId;
    private String userName;
    private int bidId;

    public CommentResponse(Comment comment){
        this.commentId = comment.getCommentId();
        this.content = comment.getCommentContent();
        this.commentCreatedAt = comment.getCommentCreateAt();
        if(comment.getUser()!=null){
            this.userId = comment.getUser().getId();
            this.userName = comment.getUser().getName();
        }
        if(comment.getBid()!=null){
            this.bidId = comment.getBid().getBidId();
        }
    }
}

package com.nara.aivleTK.dto.board;

import com.nara.aivleTK.domain.board.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BoardResponse {
    private Integer id;
    private String title;
    private String userName;
    private String content;
    private String category;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId()).title(board.getTitle())
                .userName(board.getUser().getName()).content(board.getContent())
                .category(board.getCategory()).likeCount(board.getLikeCount())
                .createdAt(board.getCreatedAt()).updatedAt(board.getUpdatedAt()).build();
    }
    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.userName = board.getUser().getName();
        this.category = board.getCategory();
        this.createdAt = board.getCreatedAt();
        this.content = board.getContent();
        this.likeCount = board.getLikeCount();
        this.viewCount = board.getViewCount();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }
}

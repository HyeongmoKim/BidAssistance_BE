package com.nara.aivleTK.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    private String Content;
    private Integer userId;
    private String bidId;
}

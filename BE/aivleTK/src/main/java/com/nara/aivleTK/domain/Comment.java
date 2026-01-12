package com.nara.aivleTK.domain;

import com.nara.aivleTK.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Integer commentId;

    @Column(name="comment_content", columnDefinition = "TEXT", nullable = false)
    private String commentContent;

    @Column(name="comment_date",nullable = false)
    private LocalDateTime commentCreateAt;

    @ManyToOne
    @JoinColumn(name="bid_id",nullable = false)
    private Bid bid;

    @ManyToOne
    @JoinColumn(name="users_user_id",nullable = false)
    private User user;

    @PrePersist
    public void onCreate() {
        this.commentCreateAt = LocalDateTime.now();
    }
}


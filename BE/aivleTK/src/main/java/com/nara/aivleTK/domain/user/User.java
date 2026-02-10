package com.nara.aivleTK.domain.user;

import com.nara.aivleTK.domain.company.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(length = 25, nullable = false)
    private String email;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "question", nullable = false)
    private Integer question;

    @Column(length = 50, nullable = false)
    private String answer;

    private LocalDate birth;

    @Column(name = "role")
    private Integer role; // 00: 일반 유저 01: 기업 10: 관리자 11: 휴면

    @Builder.Default
    @Column(name = "expert_points")
    private Integer expertPoints = 0; // 활동 포인트

    public int calcExpertLevel() {
        int pts = (this.expertPoints != null) ? this.expertPoints : 0;
        if (pts == -100)
            return 0; // 관리자
        return Math.max(1, Math.min(5, (pts / 100) + 1));
    }

    public void addExpertPoints(int points) {
        this.expertPoints = (this.expertPoints == null ? 0 : this.expertPoints) + points;
    }
}

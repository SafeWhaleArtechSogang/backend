package com.safewhale.user.domain;

import com.safewhale.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String provider;
    @Column(name = "provider_id", nullable = false, length = 128)
    private String providerId;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(length = 100)
    private String major;
    @Column(name = "student_no", length = 20)
    private String studentNo;
    @Column(name = "push_token")
    private String pushToken;

    public User(String provider, String providerId, String name) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
    }

    public void updateProfile(String name, String major, String studentNo) {
        this.name = name;
        this.major = major;
        this.studentNo = studentNo;
    }
}

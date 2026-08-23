package com.safewhale.admin.domain;

import com.safewhale.common.entity.BaseTimeEntity;
import com.safewhale.department.domain.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 50)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public Admin(String loginId, String passwordHash, String name, Department department) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.department = department;
    }
}

package com.safewhale.admin.repository;

import com.safewhale.admin.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByLoginId(String loginId);
    Optional<Admin> findByUserId(Long userId);
}

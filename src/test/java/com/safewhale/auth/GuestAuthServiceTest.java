package com.safewhale.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.safewhale.admin.repository.AdminRepository;
import com.safewhale.auth.service.GuestAuthService;
import com.safewhale.common.exception.BusinessException;
import com.safewhale.common.security.PrincipalType;
import com.safewhale.department.domain.Department;
import com.safewhale.department.repository.DepartmentRepository;
import com.safewhale.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GuestAuthServiceTest {
    @Autowired GuestAuthService guestAuthService;
    @Autowired UserRepository userRepository;
    @Autowired AdminRepository adminRepository;
    @Autowired DepartmentRepository departmentRepository;

    @Test
    void issuesStudentAndAdminTokensWithValidCodeAndReusesAccounts() {
        departmentRepository.save(new Department("게스트 테스트 부서", "GUEST_TEST"));

        var student = guestAuthService.login(PrincipalType.USER, "test-guest-code");
        var admin = guestAuthService.login(PrincipalType.ADMIN, "test-guest-code");
        guestAuthService.login(PrincipalType.ADMIN, "test-guest-code");

        assertThat(student.principalType()).isEqualTo(PrincipalType.USER);
        assertThat(admin.principalType()).isEqualTo(PrincipalType.ADMIN);
        assertThat(userRepository.findByProviderAndProviderId("GUEST", "judge-student")).get()
                .satisfies(user -> assertThat(user.isProfileCompleted()).isTrue());
        assertThat(adminRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsWrongOrMissingCode() {
        assertThatThrownBy(() -> guestAuthService.login(PrincipalType.ADMIN, "wrong"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> guestAuthService.login(PrincipalType.USER, null))
                .isInstanceOf(BusinessException.class);
    }
}

package kr.co.amateurs.server.config.jwt;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomUserDetailsTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        // given
        testUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("김테스트")
                .password("password123")
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .build();
    }


    @Test
    void User_엔티티를_UserDetails로_변환할_수_있다() {

        // when
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        // then
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}

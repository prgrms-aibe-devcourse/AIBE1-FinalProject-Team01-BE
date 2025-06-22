package kr.co.amateurs.server.config.jwt;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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

    @Test
    void Role값에_ROLE_접두사가_추가되어_권한이_생성된다() {
        // given
        User studentUser = User.builder()
                .email("student@test.com")
                .nickname("studentnick")
                .role(Role.STUDENT)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(studentUser);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void getUser로_원본_User_엔티티에_접근할_수_있다() {
        // when
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        // then
        assertThat(userDetails.getUser()).isEqualTo(testUser);
        assertThat(userDetails.getUser().getNickname()).isEqualTo("testnick");
        assertThat(userDetails.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(userDetails.getUser().getName()).isEqualTo("김테스트");
    }
}

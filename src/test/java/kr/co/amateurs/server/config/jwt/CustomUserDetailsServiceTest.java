package kr.co.amateurs.server.config.jwt;

import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

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
    void 존재하는_이메일로_사용자를_로드할_수_있다() {
        // given
        given(userService.findByEmail("test@test.com")).willReturn(testUser);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@test.com");

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
    }


}

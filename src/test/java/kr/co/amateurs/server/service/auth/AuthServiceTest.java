package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void 정상적인_유저_등록_시_201_응답을_반환한다() {
        // given
        SignupRequestDto request = SignupRequestDto.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("testname")
                .password("password123")
                .build();

        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("testname")
                .password(encodedPassword)
                .role(Role.GUEST)
                .providerType(ProviderType.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponseDto response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.nickname()).isEqualTo("testnick");

        verify(userService).validateEmailDuplicate("test@test.com");
        verify(userService).validateNicknameDuplicate("testnick");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
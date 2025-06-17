package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입이 성공적으로 처리된다")
    void signup_Success() {
        // given
        SignupRequestDto request = new SignupRequestDto();

        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email("test@test.com")
                .nickname("testnick")
                .name("testname")
                .password(encodedPassword)
                .role(Role.STUDENT)
                .providerType(ProviderType.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        // when
        SignupResponseDto response = authService.signup(request);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("testnick");

        verify(userService).validateEmailDuplicate(anyString());
        verify(userService).validateNicknameDuplicate(anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

}

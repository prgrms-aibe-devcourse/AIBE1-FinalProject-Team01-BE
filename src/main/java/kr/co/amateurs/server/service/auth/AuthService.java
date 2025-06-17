package kr.co.amateurs.server.service.auth;

import kr.co.amateurs.server.domain.dto.auth.SignupRequestDto;
import kr.co.amateurs.server.domain.dto.auth.SignupResponseDto;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.ProviderType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SignupResponseDto signup(SignupRequestDto request){
        userService.validateEmailDuplicate(request.getEmail());
        userService.validateNicknameDuplicate(request.getNickname());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .name(request.getName())
                .password(encodedPassword)
                .role(Role.STUDENT)
                .providerType(ProviderType.LOCAL)
                .build();

        User savedUser = userRepository.save(user);

        return SignupResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
}

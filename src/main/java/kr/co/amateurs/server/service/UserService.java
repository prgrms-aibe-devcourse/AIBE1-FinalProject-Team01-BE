package kr.co.amateurs.server.service;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void validateEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw ErrorCode.DUPLICATE_EMAIL.get();
        }
    }

    public void validateNicknameDuplicate(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw ErrorCode.DUPLICATE_NICKNAME.get();
        }
    }

    public User saveUser (User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return Optional.ofNullable(customUserDetails.getUser());
        }
        return Optional.empty();
    }

    public boolean isEmailAvailable(String email) {
        validateEmailFormat(email);
        return !userRepository.existsByEmail(email);
    }

    public boolean isNicknameAvailable(String nickname) {
        validateNicknameFormat(nickname);
        return !userRepository.existsByNickname(nickname);
    }

    private void validateEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw ErrorCode.EMPTY_EMAIL.get();
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw ErrorCode.INVALID_EMAIL_FORMAT.get();
        }
    }

    private void validateNicknameFormat(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw ErrorCode.EMPTY_NICKNAME.get();
        }

        if (nickname.length() < 2 || nickname.length() > 20) {
            throw ErrorCode.INVALID_NICKNAME_LENGTH.get();
        }
    }
}

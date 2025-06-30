package kr.co.amateurs.server.service;

import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.user.*;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Topic;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User saveUser(User user) {
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

    public User getCurrentLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new CustomException(ErrorCode.ANONYMOUS_USER);
        }

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUser();
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

    public UserProfileResponseDto getCurrentUserProfile() {
        Long userId = getCurrentUser()
                .map(User::getId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        User user = userRepository.findById(userId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        return UserProfileResponseDto.from(user);
    }

    public UserBasicProfileEditResponseDto updateBasicProfile(UserBasicProfileEditRequestDto request) {
        User currentUser = getCurrentLoginUser();

        User userFromDb = userRepository.findById(currentUser.getId())
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        if(request.nickname() != null && !request.nickname().equals(userFromDb.getNickname())) {
            validateEmailDuplicate(request.nickname());
            validateNicknameFormat(request.nickname());
        }

        userFromDb.updateBasicProfile(
                request.name(),
                request.nickname(),
                request.imageUrl()
        );

        User savedUser = userRepository.save(userFromDb);
        return UserBasicProfileEditResponseDto.from(savedUser);
    }

    public UserPasswordEditResponseDto updatePassword(UserPasswordEditRequestDto request) {
        User currentUser = getCurrentLoginUser();

        User userFromDb = userRepository.findById(currentUser.getId())
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        validateCurrentPassword(userFromDb, request.currentPassword());

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        userFromDb.updatePassword(encodedPassword);

        userRepository.save(userFromDb);

        return UserPasswordEditResponseDto.builder()
                .message("비밀번호가 성공적으로 변경되었습니다")
                .build();
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw ErrorCode.EMPTY_CURRENT_PASSWORD.get();
        }

        if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw ErrorCode.INVALID_CURRENT_PASSWORD.get();
        }
    }

    public UserTopicsEditResponseDto updateTopics(UserTopicsEditRequestDto request) {
        User currentUser = getCurrentLoginUser();

        User userFromDb = userRepository.findById(currentUser.getId())
                .orElseThrow(ErrorCode.USER_NOT_FOUND);

        validateTopicsCount(request.topics());

        userFromDb.getUserTopics().clear();
        userFromDb.addUserTopics(request.topics());

        User savedUser = userRepository.save(userFromDb);
        return UserTopicsEditResponseDto.from(savedUser);
    }

    private void validateTopicsCount(Set<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            throw ErrorCode.TOPICS_REQUIRED.get();
        }

        if (topics.size() > 3) {
            throw ErrorCode.TOPICS_TOO_MANY.get();
        }
    }
}

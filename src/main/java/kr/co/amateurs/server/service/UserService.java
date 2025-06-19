package kr.co.amateurs.server.service;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public User save (User user) {
        return userRepository.save(user);
    }
}

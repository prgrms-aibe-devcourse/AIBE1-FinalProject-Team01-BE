package kr.co.amateurs.server.domain.entity.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    STUDENT("ROLE_STUDENT", "수강생"),
    ADMIN("ROLE_ADMIN", "관리자"),
    GUEST("ROLE_GUEST", "게스트");

    private final String key;
    private final String title;
}

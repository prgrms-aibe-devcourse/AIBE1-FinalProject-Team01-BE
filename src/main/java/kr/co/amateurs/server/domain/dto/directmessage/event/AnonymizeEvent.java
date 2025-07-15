package kr.co.amateurs.server.domain.dto.directmessage.event;

import kr.co.amateurs.server.domain.entity.user.User;

public record AnonymizeEvent(
        User user
) {
}

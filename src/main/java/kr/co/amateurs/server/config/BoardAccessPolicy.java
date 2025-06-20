package kr.co.amateurs.server.config;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class BoardAccessPolicy {
    private static final Map<BoardType, Set<Role>> BOARD_ACCESS_MAP = Map.of(
            BoardType.FREE, Set.of(Role.GUEST, Role.STUDENT, Role.ADMIN),
            BoardType.QNA, Set.of(Role.STUDENT, Role.ADMIN),
            BoardType.RETROSPECT, Set.of(Role.STUDENT, Role.ADMIN),
            BoardType.MARKET, Set.of(Role.ADMIN),
            BoardType.GATHER, Set.of(Role.ADMIN),
            BoardType.MATCH, Set.of(Role.ADMIN),
            BoardType.PROJECT_HUB, Set.of(Role.ADMIN),
            BoardType.INFO, Set.of(Role.ADMIN),
            BoardType.REVIEW, Set.of(Role.ADMIN)
    );

    public static boolean canAccess(Role userRole, BoardType boardType) {
        return BOARD_ACCESS_MAP.getOrDefault(boardType, Set.of()).contains(userRole);
    }

    public static void validateAccess(Role userRole, BoardType boardType) {
        if (!canAccess(userRole, boardType)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public static void validatePostAccess(Role userRole, Post post) {
        validateAccess(userRole, post.getBoardType());
    }
}

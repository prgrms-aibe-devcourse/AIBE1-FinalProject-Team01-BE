package kr.co.amateurs.server.annotation.boardaccess;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.enums.BoardCategory;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.OperationType;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class BoardAccessPolicy {
    private final Map<BoardCategory, Set<Role>> READ_ACCESS_MAP = Map.of(
            BoardCategory.COMMUNITY, Set.of(Role.GUEST, Role.STUDENT, Role.ADMIN),
            BoardCategory.TOGETHER, Set.of(Role.STUDENT, Role.ADMIN),
            BoardCategory.IT, Set.of(Role.ANONYMOUS,Role.GUEST, Role.STUDENT, Role.ADMIN),
            BoardCategory.PROJECT, Set.of(Role.ANONYMOUS,Role.GUEST, Role.STUDENT, Role.ADMIN)
    );

    private final Map<BoardCategory, Set<Role>> WRITE_ACCESS_MAP = Map.of(
            BoardCategory.COMMUNITY, Set.of(Role.STUDENT, Role.ADMIN),
            BoardCategory.TOGETHER, Set.of(Role.STUDENT, Role.ADMIN),
            BoardCategory.IT, Set.of(Role.STUDENT, Role.ADMIN),
            BoardCategory.PROJECT, Set.of(Role.STUDENT, Role.ADMIN)
    );

    private final Map<BoardType, BoardCategory> BOARD_TYPE_CATEGORY_MAP = Map.of(
            BoardType.FREE, BoardCategory.COMMUNITY,
            BoardType.QNA, BoardCategory.COMMUNITY,
            BoardType.RETROSPECT, BoardCategory.COMMUNITY,

            BoardType.MARKET, BoardCategory.TOGETHER,
            BoardType.GATHER, BoardCategory.TOGETHER,
            BoardType.MATCH, BoardCategory.TOGETHER,

            BoardType.INFO, BoardCategory.IT,
            BoardType.REVIEW, BoardCategory.IT,

            BoardType.PROJECT_HUB, BoardCategory.PROJECT
    );

    public boolean canAccess(Role userRole, BoardCategory boardCategory, OperationType operationType) {
        Map<BoardCategory, Set<Role>> accessMap = OperationType.WRITE.equals(operationType)
                ? WRITE_ACCESS_MAP
                : READ_ACCESS_MAP;

        return accessMap.getOrDefault(boardCategory, Set.of()).contains(userRole);
    }

    public void validateAccess(Role userRole, BoardType boardType, OperationType operationType) {
        BoardCategory category = BOARD_TYPE_CATEGORY_MAP.get(boardType);

        if (!canAccess(userRole, category, operationType)) {
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }

    public void boardTypeInCategory(BoardType targetBoardType, BoardCategory category) {
        BoardCategory actualCategory = BOARD_TYPE_CATEGORY_MAP.get(targetBoardType);
        if (!(actualCategory == category)){
            throw ErrorCode.ACCESS_DENIED.get();
        }
    }
}

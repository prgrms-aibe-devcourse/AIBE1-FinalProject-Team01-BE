package kr.co.amateurs.server.service.report.handler;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.report.enums.ReportTarget;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReportTargetHandlerFactory {
    private final Map<ReportTarget, ReportTargetHandler> handlers;

    public ReportTargetHandlerFactory(PostTargetHandler postHandler,
                                      CommentTargetHandler commentHandler) {
        this.handlers = Map.of(
                ReportTarget.POST, postHandler,
                ReportTarget.COMMENT, commentHandler
        );
    }

    public ReportTargetHandler getHandler(ReportTarget target) {
        ReportTargetHandler handler = handlers.get(target);
        if (handler == null) {
            throw ErrorCode.NOT_FOUND.get();
        }
        return handler;
    }
}

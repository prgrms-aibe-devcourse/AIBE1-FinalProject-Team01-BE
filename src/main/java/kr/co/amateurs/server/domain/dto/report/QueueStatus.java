package kr.co.amateurs.server.domain.dto.report;

public record QueueStatus(
        int queueSize,
        boolean isRunning,
        boolean isThreadAlive
) {}

package kr.co.amateurs.server.domain.dto.post;

import lombok.Getter;

@Getter
public class PostViewedEvent {
    private final Long postId;
    private final String ipAddress;

    public PostViewedEvent(Long postId, String ipAddress) {
        this.postId = postId;
        this.ipAddress = ipAddress;
    }
}

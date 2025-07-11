package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.dto.post.PostViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PostViewEventHandler {

    private final ViewCountService viewCountService;

    @Async
    @EventListener
    public void handlePostViewed(PostViewedEvent event) {
        viewCountService.incrementViewCount(event.getPostId(), event.getIpAddress());
    }
}

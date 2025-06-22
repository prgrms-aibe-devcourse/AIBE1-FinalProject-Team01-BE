package kr.co.amateurs.server.domain.dto.directmessage;

import kr.co.amateurs.server.domain.dto.common.PageInfo;
import kr.co.amateurs.server.domain.entity.directmessage.DirectMessage;
import org.springframework.data.domain.Page;

import java.util.List;

public record DirectMessagePageResponse(
        List<DirectMessageResponse> messages,
        PageInfo pageInfo
) {
    public static DirectMessagePageResponse from(Page<DirectMessage> page) {
        List<DirectMessageResponse> messageResponses = page.getContent().stream()
                .map(DirectMessageResponse::fromCollection)
                .toList();
        PageInfo info = PageInfo.from(page);

        return new DirectMessagePageResponse(messageResponses, info);
    }
}

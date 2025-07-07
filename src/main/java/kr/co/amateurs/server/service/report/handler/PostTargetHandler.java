package kr.co.amateurs.server.service.report.handler;

import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.report.Report;
import kr.co.amateurs.server.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostTargetHandler extends ReportTargetHandler {

    private final PostRepository postRepository;

    @Override
    public boolean isAlreadyBlinded(Report report) {
        return report.getPost() != null && report.getPost().getIsBlinded();
    }

    @Override
    public Long getTargetId(Report report) {
        return report.getPost().getId();
    }

    @Override
    public void blindTarget(Report report) {
        Post post = postRepository.findById(report.getPost().getId())
                .orElseThrow(ErrorCode.POST_NOT_FOUND);
        post.updateBlinded(true);
        postRepository.save(post);
    }

    @Override
    public String getTargetType() {
        return "POST";
    }
}

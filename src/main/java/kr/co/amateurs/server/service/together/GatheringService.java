package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.dto.together.gathering.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.gathering.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.gathering.UpdateGatheringPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<GatheringPostResponseDTO> getGatheringPostList() {
        return gatheringRepository.findAll()
                .stream()
                .map(gp -> {
                    Post post = gp.getPost();
                    return new GatheringPostResponseDTO(
                            post.getId(),
                            post.getUser().getId(),
                            post.getTitle(),
                            post.getContent(),
                            post.getTags(),
                            post.getViewCount(),
                            post.getLikeCount(),
                            gp.getGatheringType(),
                            gp.getStatus(),
                            gp.getHeadCount(),
                            gp.getPlace(),
                            gp.getPeriod(),
                            gp.getSchedule(),
                            post.getCreatedAt(),
                            post.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }


    public GatheringPostResponseDTO getGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = gp.getPost();
        return new GatheringPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                gp.getGatheringType(),
                gp.getStatus(),
                gp.getHeadCount(),
                gp.getPlace(),
                gp.getPeriod(),
                gp.getSchedule(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional
    public GatheringPostResponseDTO createGatheringPost(GatheringPostRequestDTO dto) {

        //유저 인증 생기면 request dto에서 userId 제거하고 글 작성 유저 정보로 넣겠습니다.

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.userId()));



        Post post = Post.builder()
                .user(user)
                .boardType(BoardType.GATHER)
                .title(dto.title())
                .content(dto.content())
                .tags(dto.tags())
                .build();
        Post savedPost = postRepository.save(post);

        GatheringPost gp = GatheringPost.builder()
                .post(savedPost)
                .gatheringType(dto.gatheringType())
                .status(GatheringStatus.RECRUITING)
                .headCount(dto.headCount())
                .place(dto.place())
                .period(dto.period())
                .schedule(dto.schedule())
                .build();
        GatheringPost savedGp = gatheringRepository.save(gp);

        return new GatheringPostResponseDTO(
                savedPost.getId(),
                savedPost.getUser().getId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                savedPost.getTags(),
                savedPost.getViewCount(),
                savedPost.getLikeCount(),
                savedGp.getGatheringType(),
                savedGp.getStatus(),
                savedGp.getHeadCount(),
                savedGp.getPlace(),
                savedGp.getPeriod(),
                savedGp.getSchedule(),
                savedPost.getCreatedAt(),
                savedPost.getUpdatedAt()
        );
    }


    /*
        JPA에서 update 처리하는 법 조사중
     */
    @Transactional
    public void updateGatheringPost(Long gatheringId, UpdateGatheringPostRequestDTO dto) {
//        GatheringPost existing = gatheringRepository.findById(gatheringId)
//                .orElseThrow(() -> new IllegalArgumentException("GatheringPost not found: " + gatheringId));
//
//        Post post = existing.getPost();
//        post.setTitle(dto.title());
//        post.setContent(dto.content());
//        post.setTags(dto.tags());
//
//        existing.setGatheringType(dto.gatheringType());
//        existing.setStatus(dto.status());
//        existing.setHeadCount(dto.headCount());
//        existing.setPlace(dto.place());
//        existing.setPeriod(dto.period());
//        existing.setRequiredSkills(dto.requiredSkills());
    }

    @Transactional
    public void deleteGatheringPost(Long gatheringId) {
        gatheringRepository.deleteById(gatheringId);
    }
}


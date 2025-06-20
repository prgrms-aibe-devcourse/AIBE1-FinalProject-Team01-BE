package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<GatheringPostResponseDTO> getGatheringPostList(String keyword, int page, int size, SortType sortType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GatheringPost> gpPage = switch (sortType) {
            case LATEST -> gatheringRepository.findAllByKeyword(keyword, pageable);
            case POPULAR -> gatheringRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
            case VIEW_COUNT -> gatheringRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
        };
        return convertToDTO(gpPage);
    }


    public GatheringPostResponseDTO getGatheringPost(Long id) {
        GatheringPost gp = gatheringRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = gp.getPost();
        return convertToDTO(gp, post);
    }


    //TODO - validation 추가 필요
    @Transactional
    public GatheringPostResponseDTO createGatheringPost(GatheringPostRequestDTO dto) {

        //TODO - 유저 인증 생기면 request dto에서 userId 제거하고 유저 인증 관련 로직으로 수정할 예정입니다.
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

        return convertToDTO(savedGp, savedPost);
    }


    //TODO - validation 추가 필요
    @Transactional
    public void updateGatheringPost(Long gatheringId, GatheringPostRequestDTO dto) {
        GatheringPost gp = gatheringRepository.findById(gatheringId).orElseThrow(() -> new IllegalArgumentException("Gathering Post not found: " + gatheringId));
        Post post = gp.getPost();
        CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());

        post.update(updatePostDTO);
        gp.update(dto);
    }

    @Transactional
    public void deleteGatheringPost(Long gatheringId) {
        gatheringRepository.deleteById(gatheringId);
    }

}


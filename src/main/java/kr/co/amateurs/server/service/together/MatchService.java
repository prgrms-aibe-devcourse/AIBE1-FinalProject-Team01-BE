package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.UpdatePostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<MatchPostResponseDTO> getMatchPostList(String keyword, int page, int size, String sortType) {
        Pageable pageable = createPageable(page, size, sortType);
        Page<MatchingPost> mpPage;
        if(keyword == null || keyword.isBlank()){
            mpPage = matchRepository.findAll(pageable);
        }
        else{
            mpPage = matchRepository.findAllByKeyword(keyword.trim(), pageable);
        }
        return mpPage.map(mp -> {
            Post post = mp.getPost();
            return convertToDTO(mp, post);
        });
    }


    public MatchPostResponseDTO getMatchPost(Long id) {
        MatchingPost mp = matchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = mp.getPost();
        return convertToDTO(mp, post);
    }


    //TODO - validation 추가 필요
    @Transactional
    public MatchPostResponseDTO createMatchPost(MatchPostRequestDTO dto) {

        //TODO - 유저 인증 생기면 request dto에서 userId 제거하고 유저 인증 관련 로직으로 수정할 예정입니다.
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.userId()));

        Post post = Post.builder()
                .user(user)
                .boardType(BoardType.MATCH)
                .title(dto.title())
                .content(dto.content())
                .tags(dto.tags())
                .build();
        Post savedPost = postRepository.save(post);

        MatchingPost mp = MatchingPost.builder()
                .post(savedPost)
                .matchingType(dto.matchingType())
                .status(MatchingStatus.OPEN)
                .expertiseAreas(dto.expertiseArea())
                .build();
        MatchingPost savedMp = matchRepository.save(mp);

        return convertToDTO(savedMp, savedPost);
    }

    //TODO - validation 추가 필요
    @Transactional
    public void updateMatchPost(Long matchId, MatchPostRequestDTO dto) {
        MatchingPost mp = matchRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Match Post not found: " + matchId));
        Post post = mp.getPost();
        UpdatePostRequestDTO updatePostDTO = new UpdatePostRequestDTO(dto.title(), dto.content(), dto.tags());

        mp.update(dto);
        post.updatePost(updatePostDTO);

    }

    @Transactional
    public void deleteMatchPost(Long matchId) {
        matchRepository.deleteById(matchId);
    }

    private MatchPostResponseDTO convertToDTO(MatchingPost mp, Post post) {
        return new MatchPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mp.getMatchingType(),
                mp.getStatus(),
                mp.getExpertiseAreas(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    //TODO - 커뮤니티 병합 시 SortType 수정 예정
    private Pageable createPageable(int page, int pageSize, String sortType) {
        Sort sort = switch (sortType) {
            case "LATEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "POPULAR" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "VIEW_COUNT" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        return PageRequest.of(page, pageSize, sort);
    }
}

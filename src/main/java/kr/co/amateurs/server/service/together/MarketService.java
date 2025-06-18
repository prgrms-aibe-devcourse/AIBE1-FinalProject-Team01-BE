package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.UpdatePostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketRepository marketRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Page<MarketPostResponseDTO> getMarketPostList(String keyword, int page, int size, String sortType) {
        Pageable pageable = createPageable(page, size, sortType);
        Page<MarketItem> miPage;
        if(keyword == null || keyword.isBlank()){
            miPage = marketRepository.findAll(pageable);
        }
        else{
            miPage = marketRepository.findAllByKeyword(keyword.trim(), pageable);
        }
        return miPage.map(mi -> {
            Post post = mi.getPost();
            return convertToDTO(mi, post);
        });
    }


    public MarketPostResponseDTO getMarketPost(Long id) {
        MarketItem mi = marketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = mi.getPost();
        return convertToDTO(mi, post);
    }


    //TODO - validation 추가 필요
    @Transactional
    public MarketPostResponseDTO createMarketPost(MarketPostRequestDTO dto) {

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

        MarketItem mi = MarketItem.builder()
                .post(savedPost)
                .status(MarketStatus.SELLING)
                .price(dto.price())
                .place(dto.place())
                .build();
        MarketItem savedMp = marketRepository.save(mi);

        return convertToDTO(savedMp, savedPost);
    }

    //TODO - validation 추가 필요
    @Transactional
    public void updateMarketPost(Long marketId, MarketPostRequestDTO dto) {
        MarketItem mi = marketRepository.findById(marketId).orElseThrow(() -> new IllegalArgumentException("Market Post not found: " + marketId));
        Post post = mi.getPost();
        UpdatePostRequestDTO updatePostDTO = new UpdatePostRequestDTO(dto.title(), dto.content(), dto.tags());

        post.updatePost(updatePostDTO);
        mi.update(dto);

    }

    @Transactional
    public void deleteMarketPost(Long marketId) {
        marketRepository.deleteById(marketId);
    }

    private MarketPostResponseDTO convertToDTO(MarketItem mi, Post post) {
        return new MarketPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mi.getStatus(),
                mi.getPrice(),
                mi.getPlace(),
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

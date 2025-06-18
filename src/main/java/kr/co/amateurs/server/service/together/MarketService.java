package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.domain.dto.together.market.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.market.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.market.UpdateMarketPostRequestDTO;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketRepository marketRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<MarketPostResponseDTO> getMarketPostList() {
        return marketRepository.findAll()
                .stream()
                .map(mp -> {
                    Post post = mp.getPost();
                    return convertToDTO(mp, post);
                })
                .collect(Collectors.toList());
    }


    public MarketPostResponseDTO getMarketPost(Long id) {
        MarketItem mp = marketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = mp.getPost();
        return convertToDTO(mp, post);
    }

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

        MarketItem mp = MarketItem.builder()
                .post(savedPost)
                .status(MarketStatus.SELLING)
                .price(dto.price())
                .place(dto.place())
                .build();
        MarketItem savedMp = marketRepository.save(mp);

        return convertToDTO(savedMp, savedPost);
    }


    /*
        JPA에서 update 처리하는 법 조사중
     */
    @Transactional
    public void updateMarketPost(Long marketId, UpdateMarketPostRequestDTO dto) {
//        MarketPost existing = marketRepository.findById(marketId)
//                .orElseThrow(() -> new IllegalArgumentException("MarketPost not found: " + marketId));
//
//        Post post = existing.getPost();
//        post.setTitle(dto.title());
//        post.setContent(dto.content());
//        post.setTags(dto.tags());
//
//        existing.setMarketType(dto.marketType());
//        existing.setStatus(dto.status());
//        existing.setHeadCount(dto.headCount());
//        existing.setPlace(dto.place());
//        existing.setPeriod(dto.period());
//        existing.setRequiredSkills(dto.requiredSkills());
    }

    @Transactional
    public void deleteMarketPost(Long marketId) {
        marketRepository.deleteById(marketId);
    }

    private MarketPostResponseDTO convertToDTO(MarketItem mp, Post post) {
        return new MarketPostResponseDTO(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getViewCount(),
                post.getLikeCount(),
                mp.getStatus(),
                mp.getPrice(),
                mp.getPlace(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

}

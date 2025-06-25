package kr.co.amateurs.server.service.together;

import jakarta.transaction.Transactional;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MarketStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.MarketPostResponseDTO.convertToDTO;


@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketRepository marketRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PageResponseDTO<MarketPostResponseDTO> getMarketPostList(String keyword, PaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        Page<MarketItem> miPage = switch (paginationParam.getField()) {
            case LATEST -> marketRepository.findAllByKeyword(keyword, pageable);
            case POPULAR -> marketRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
            case MOST_VIEW -> marketRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
            default -> marketRepository.findAllByKeyword(keyword, pageable);
        };
        return convertPageToDTO(convertToDTO(miPage));
    }


    public MarketPostResponseDTO getMarketPost(Long id) {
        MarketItem mi = marketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = mi.getPost();
        return convertToDTO(mi, post);
    }


    @Transactional
    public MarketPostResponseDTO createMarketPost(CustomUserDetails currentUser, MarketPostRequestDTO dto) {

        Post post = Post.builder()
                .user(currentUser.getUser())
                .boardType(BoardType.MARKET)
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

    @Transactional
    public void updateMarketPost(CustomUserDetails currentUser, Long postId, MarketPostRequestDTO dto) {
        MarketItem mi = marketRepository.findByPostId(postId);
        if (mi == null) {
            throw new IllegalArgumentException("Market Post not found: " + postId);
        }
        Post post = mi.getPost();
        if(currentUser.getUser().getId().equals(post.getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
            post.update(updatePostDTO);
            mi.update(dto);
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You don't have permission to update this post");
        }


    }

    @Transactional
    public void deleteMarketPost(CustomUserDetails currentUser, Long marketId) {
        MarketItem mi = marketRepository.findByPostId(marketId);
        Post post = mi.getPost();
        if(currentUser.getUser().getId().equals(post.getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            marketRepository.deleteById(mi.getId());
            postRepository.deleteById(post.getId());
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You don't have permission to delete this post");
        }
    }


}

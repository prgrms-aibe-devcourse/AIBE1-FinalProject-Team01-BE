package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO.*;
import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final GatheringRepository gatheringRepository;
    private final MarketRepository marketRepository;
    private final MatchRepository matchRepository;

    public PageResponseDTO<BookmarkResponseDTO> getBookmarkPostList(Long userId, PaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        Page<Bookmark> bookmarkList = switch(paginationParam.getField()){
            case LATEST -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
            case POPULAR -> bookmarkRepository.getBookmarkPostByUserOrderByLikeCountDesc(userId, pageable);
            case MOST_VIEW -> bookmarkRepository.getBookmarkPostByUserOrderByViewCountDesc(userId, pageable);
            default -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
        };
        return convertPageToDTO(bookmarkList.map(this::convertToDTO));
    }

    public BookmarkResponseDTO addBookmarkPost(Long userId, Long postId) {
        User currentUser = userRepository.findById(userId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        Bookmark newBookmark = Bookmark.builder()
                .user(currentUser)
                .post(post)
                .build();
        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);
        return convertToDTO(savedBookmark);
    }

    public void removeBookmarkPost(Long userId, Long postId) {
        bookmarkRepository.deleteByUserAndPost(userId, postId);
    }

    private BookmarkResponseDTO convertToDTO(Bookmark bookmark) {
        Post p = bookmark.getPost();
        Long postId = p.getId();
        BoardType boardType = p.getBoardType();
        return switch (boardType){
            case GATHER -> {
                GatheringPost gp = gatheringRepository.findByPostId(postId);
                yield convertToGatheringDTO(bookmark, gp);
            }
            case MARKET -> {
                MarketItem mi = marketRepository.findByPostId(postId);
                yield convertToMarketDTO(bookmark, mi);
            }
            case MATCH -> {
                MatchingPost mp = matchRepository.findByPostId(postId);
                yield convertToMatchingDTO(bookmark, mp);
            }
            default -> convertToPostDTO(bookmark);
        };
    }

}

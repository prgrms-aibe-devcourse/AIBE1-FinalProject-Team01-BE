package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO.*;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final GatheringRepository gatheringRepository;
    private final MarketRepository marketRepository;
    private final MatchRepository matchRepository;

    public Page<BookmarkResponseDTO> getBookmarkPostList(Long userId, int page, int size, SortType sortType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bookmark> bookmarkList = switch(sortType){
            case LATEST -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
            case POPULAR -> bookmarkRepository.getBookmarkPostByUserOrderByLikeCountDesc(userId, pageable);
            case VIEW_COUNT -> bookmarkRepository.getBookmarkPostByUserOrderByViewCountDesc(userId, pageable);
        };
        return bookmarkList.map(this::convertToDTO);
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

    public List<PostContentData> getBookmarkedPosts(Long userId) {
        try {
            List<Bookmark> bookmarks = bookmarkRepository.findTop3ByUserIdOrderByCreatedAtDesc(userId);
            return bookmarks.stream().map(bookmark
                    -> new PostContentData(bookmark.getPost().getId(), bookmark.getPost().getTitle(), bookmark.getPost().getContent(), "bookmark")).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}

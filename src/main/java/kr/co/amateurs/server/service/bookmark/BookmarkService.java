package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.bookmark.*;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MarketItem;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.together.MarketRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.Collections;
import java.util.List;

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

    private final UserService userService;

    public PageResponseDTO<BookmarkResponseDTO> getBookmarkPostList(Long userId, PaginationParam paginationParam) {
        validateUser(userId);
        Pageable pageable = paginationParam.toPageable();
        Page<Bookmark> bookmarkList = switch(paginationParam.getField()){
            case LATEST -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
            case POPULAR -> bookmarkRepository.getBookmarkPostByUserOrderByLikeCountDesc(userId, pageable);
            case MOST_VIEW -> bookmarkRepository.getBookmarkPostByUserOrderByViewCountDesc(userId, pageable);
            default -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
        };
        return convertPageToDTO(bookmarkList.map(this::convertToDTO));
    }

    @Transactional
    public BookmarkResponseDTO addBookmarkPost(Long userId, Long postId) {
        User currentUser = validateUser(userId);
        Post post = postRepository.findById(postId).orElseThrow();
        if (checkHasBookmarked(postId, userId)) {
            throw ErrorCode.DUPLICATE_BOOKMARK.get();
        }

        Bookmark newBookmark = Bookmark.builder()
                .user(currentUser)
                .post(post)
                .build();
        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);
        return convertToDTO(savedBookmark);
    }

    @Transactional
    public void removeBookmarkPost(Long userId, Long postId) {
        validateUser(userId);
        bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
    }

    private BookmarkResponseDTO convertToDTO(Bookmark bookmark) {
        Post p = bookmark.getPost();
        Long postId = p.getId();
        BoardType boardType = p.getBoardType();
        return switch (boardType){
            case GATHER -> {
                GatheringPost gp = gatheringRepository.findByPostId(postId);
                yield GatheringBookmarkDTO.convertToDTO(gp);
            }
            case MARKET -> {
                MarketItem mi = marketRepository.findByPostId(postId);
                yield MarketBookmarkDTO.convertToDTO(mi);
            }
            case MATCH -> {
                MatchingPost mp = matchRepository.findByPostId(postId);
                yield MatchingBookmarkDTO.convertToDTO(mp);
            }
            default -> PostBookmarkDTO.convertToDTO(p);
        };
    }
    public boolean checkHasBookmarked(Long postId, Long userId) {
        return bookmarkRepository
                .existsByPost_IdAndUser_Id(postId, userId);
    }

    private User validateUser(Long userId) {
        User user = userService.getCurrentLoginUser();
        Long currentId = user.getId();
        Role currentRole = user.getRole();

        if (!currentId.equals(userId) && currentRole != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인의 북마크에만 접근할 수 있습니다.");
        }

        return user;
    }
    public List<PostContentData> getBookmarkedPosts(Long userId) {
        try {
            List<Bookmark> bookmarks = bookmarkRepository.findTop3ByUserIdOrderByCreatedAtDesc(userId);
            return bookmarks.stream().map(bookmark
                    -> new PostContentData(bookmark.getPost().getId(), bookmark.getPost().getTitle(), bookmark.getPost().getContent(), "북마크")).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean hasRecentBookmarkActivity(Long userId, int days) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            return bookmarkRepository.existsByUserIdAndCreatedAtAfter(userId, since);
        } catch (Exception e) {
            return false;
        }
    }

}

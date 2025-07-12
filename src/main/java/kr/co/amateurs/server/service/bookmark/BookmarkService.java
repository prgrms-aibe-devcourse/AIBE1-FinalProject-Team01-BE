package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.ai.PostContentData;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.post.PostResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.*;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostJooqRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.post.PostStatisticsRepository;
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

import java.util.Collections;
import java.util.List;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final PostJooqRepository postJooqRepository;

    private final UserService userService;

    public PageResponseDTO<PostResponseDTO> getBookmarkPostList(PaginationParam paginationParam) {
        User user = userService.getCurrentLoginUser();
        Pageable pageable = paginationParam.toPageable();

        Page<PostResponseDTO> postResponseDTO = postJooqRepository.findPostsByType(user.getId(), pageable, "bookmarked");

        return convertPageToDTO(postResponseDTO);
    }

    @Transactional
    public BookmarkResponseDTO addBookmarkPost(Long postId) {
        User currentUser = userService.getCurrentLoginUser();
        Post post = postRepository.findById(postId).orElseThrow(ErrorCode.POST_NOT_FOUND);
        if (checkHasBookmarked(postId, currentUser.getId())) {
            throw ErrorCode.DUPLICATE_BOOKMARK.get();
        }

        Bookmark newBookmark = Bookmark.builder()
                .user(currentUser)
                .post(post)
                .build();
        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);
        return BookmarkResponseDTO.addSucceed(savedBookmark);
    }

    @Transactional
    public void removeBookmarkPost(Long postId) {
        User user = userService.getCurrentLoginUser();

        bookmarkRepository.deleteByUserIdAndPostId(user.getId(), postId);
    }

    public boolean checkHasBookmarked(Long postId, Long userId) {
        return bookmarkRepository
                .existsByPost_IdAndUser_Id(postId, userId);
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

    public Integer countBookmark(Post post){
        User user = userService.getCurrentLoginUser();
        Integer count = bookmarkRepository.countByPostAndUser(post, user);
        return count;
    }

}

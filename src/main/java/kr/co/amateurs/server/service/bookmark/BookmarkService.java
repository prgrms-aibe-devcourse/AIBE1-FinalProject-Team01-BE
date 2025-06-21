package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkRequestDTO;
import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public Page<BookmarkResponseDTO> getBookmarkPostList(Long userId, int page, int size, SortType sortType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookmarkResponseDTO> bookmarkList = bookmarkRepository.getBookmarkPostByUser(userId, pageable);
        return bookmarkList;
    }

    public BookmarkResponseDTO addBookmarkPost(Long userId, Long postId, BookmarkRequestDTO bookmarkRequestDTO) {
        BookmarkResponseDTO bookmarkResponseDTO = new BookmarkResponseDTO();
        return bookmarkResponseDTO;
    }

}

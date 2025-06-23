package kr.co.amateurs.server.service.bookmark;

import kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO;
import kr.co.amateurs.server.domain.entity.bookmark.Bookmark;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.bookmark.BookmarkResponseDTO.convertToDTO;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

//    public Page<BookmarkResponseDTO> getBookmarkPostList(Long userId, int page, int size, SortType sortType) {
//        Pageable pageable = PageRequest.of(page, size);
////        Page<Bookmark> bookmarkList = switch(sortType){
////            case LATEST -> bookmarkRepository.getBookmarkPostByUser(userId, pageable);
////            case POPULAR -> bookmarkRepository.getBookmarkPostByUserOrderByLikeCountDesc(userId, pageable);
////            case VIEW_COUNT -> bookmarkRepository.getBookmarkPostByUserOrderByViewCountDesc(userId, pageable);
////        };
//        return convertToDTO(bookmarkList);
//    }

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

}

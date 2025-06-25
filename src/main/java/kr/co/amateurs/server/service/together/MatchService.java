package kr.co.amateurs.server.service.together;


import jakarta.transaction.Transactional;
import kr.co.amateurs.server.config.jwt.CustomUserDetails;
import kr.co.amateurs.server.domain.common.ErrorCode;
import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PaginationParam;
import kr.co.amateurs.server.domain.dto.community.CommunityRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.MatchingPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.MatchingStatus;
import kr.co.amateurs.server.domain.entity.post.enums.SortType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.MatchRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static kr.co.amateurs.server.domain.dto.common.PageResponseDTO.convertPageToDTO;
import static kr.co.amateurs.server.domain.dto.together.MatchPostResponseDTO.convertToDTO;


@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PageResponseDTO<MatchPostResponseDTO> getMatchPostList(String keyword, PaginationParam paginationParam) {
        Pageable pageable = paginationParam.toPageable();
        Page<MatchingPost> mpPage = switch (paginationParam.getField()) {
            case LATEST -> matchRepository.findAllByKeyword(keyword, pageable);
            case POPULAR -> matchRepository.findAllByKeywordOrderByLikeCountDesc(keyword, pageable);
            case MOST_VIEW -> matchRepository.findAllByKeywordOrderByViewCountDesc(keyword, pageable);
            default -> matchRepository.findAllByKeyword(keyword, pageable);
        };
        return convertPageToDTO(convertToDTO(mpPage));
    }


    public MatchPostResponseDTO getMatchPost(Long id) {
        MatchingPost mp = matchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        Post post = mp.getPost();
        return convertToDTO(mp, post);
    }


    @Transactional
    public MatchPostResponseDTO createMatchPost(CustomUserDetails currentUser, MatchPostRequestDTO dto) {
        Post post = Post.builder()
                .user(currentUser.getUser())
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

    @Transactional
    public void updateMatchPost(CustomUserDetails currentUser, Long postId, MatchPostRequestDTO dto) {
        MatchingPost mp = matchRepository.findByPostId(postId);
        if(mp == null) {
            throw new IllegalArgumentException("Match Post not found: " + postId);
        }
        Post post = mp.getPost();

        if(currentUser.getUser().getId().equals(post.getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            CommunityRequestDTO updatePostDTO = new CommunityRequestDTO(dto.title(), dto.content(), dto.tags());
            mp.update(dto);
            post.update(updatePostDTO);
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You do not have permission to update this post");
        }


    }

    @Transactional
    public void deleteMatchPost(CustomUserDetails currentUser, Long postId) {
        MatchingPost mp = matchRepository.findByPostId(postId);
        Post post = mp.getPost();
        if(currentUser.getUser().getId().equals(post.getUser().getId()) || currentUser.getUser().getRole().equals(Role.ADMIN)) {
            matchRepository.deleteById(mp.getId());
            postRepository.deleteById(post.getId());
        }
        else{
            throw new CustomException(ErrorCode.ACCESS_DENIED, "You do not have permission to delete this post");
        }
    }
}

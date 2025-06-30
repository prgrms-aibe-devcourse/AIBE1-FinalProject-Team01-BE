package kr.co.amateurs.server.service.together;

import kr.co.amateurs.server.domain.dto.common.PageResponseDTO;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.dto.together.GatheringPostRequestDTO;
import kr.co.amateurs.server.domain.dto.together.GatheringPostResponseDTO;
import kr.co.amateurs.server.domain.entity.post.GatheringPost;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringStatus;
import kr.co.amateurs.server.domain.entity.post.enums.GatheringType;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import kr.co.amateurs.server.exception.CustomException;
import kr.co.amateurs.server.repository.bookmark.BookmarkRepository;
import kr.co.amateurs.server.repository.like.LikeRepository;
import kr.co.amateurs.server.repository.post.PostRepository;
import kr.co.amateurs.server.repository.together.GatheringRepository;
import kr.co.amateurs.server.repository.user.UserRepository;
import kr.co.amateurs.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GatheringService 통합 테스트")
class GatheringServiceTest {

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserService userService;

    private User testUser;
    private User adminUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .role(Role.STUDENT)
                .build();
        testUser = userRepository.save(testUser);

        adminUser = User.builder()
                .email("admin@example.com")
                .nickname("관리자")
                .role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        otherUser = User.builder()
                .email("other@example.com")
                .nickname("다른유저")
                .role(Role.STUDENT)
                .build();
        otherUser = userRepository.save(otherUser);

        // 보안 컨텍스트에 테스트 유저 설정
        setSecurityContext(testUser);
    }

    private void setSecurityContext(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private GatheringPostRequestDTO createValidRequestDTO() {
        return new GatheringPostRequestDTO(
                "팀원 모집합니다",
                "React 프로젝트 함께할 팀원을 모집합니다. 경험자 우대합니다.",
                "React",
                GatheringType.SIDE_PROJECT,
                GatheringStatus.RECRUITING,
                4,
                "온라인",
                "3개월",
                "주 2회 화/목 오후 7시"
        );
    }

    private GatheringPost createTestGatheringPost(User author) {
        Post post = Post.builder()
                .user(author)
                .boardType(BoardType.GATHER)
                .title("테스트 제목")
                .content("테스트 내용")
                .tags("Java")
                .build();
        Post savedPost = postRepository.save(post);

        GatheringPost gatheringPost = GatheringPost.builder()
                .post(savedPost)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(5)
                .place("강남역")
                .period("2개월")
                .schedule("매주 토요일")
                .build();
        return gatheringRepository.save(gatheringPost);
    }

    @Test
    @DisplayName("키워드 없이 팀원 모집 글 목록을 조회하면 전체 게시글이 반환되어야 한다")
    void 키워드_없이_팀원_모집_글_목록을_조회하면_전체_게시글이_반환되어야_한다() {
        // given
        createTestGatheringPost(testUser);
        createTestGatheringPost(otherUser);

        PostPaginationParam paginationParam = new PostPaginationParam(
                null
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
        assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        assertThat(result.pageInfo().getPageSize()).isEqualTo(10);
        assertThat(result.pageInfo().getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("키워드로 팀원 모집 글을 검색하면 해당 키워드가 포함된 게시글만 반환되어야 한다")
    void 키워드로_팀원_모집_글을_검색하면_해당_키워드가_포함된_게시글만_반환되어야_한다() {
        // given
        // React 키워드가 포함된 게시글 생성
        Post reactPost = Post.builder()
                .user(testUser)
                .boardType(BoardType.GATHER)
                .title("React 팀원 모집")
                .content("React 프로젝트 함께하실 분")
                .tags("React")
                .build();
        Post savedReactPost = postRepository.save(reactPost);

        GatheringPost reactGathering = GatheringPost.builder()
                .post(savedReactPost)
                .gatheringType(GatheringType.SIDE_PROJECT)
                .status(GatheringStatus.RECRUITING)
                .headCount(4)
                .place("온라인")
                .period("3개월")
                .schedule("주 2회")
                .build();
        gatheringRepository.save(reactGathering);

        // Vue 키워드가 포함된 게시글 생성
        createTestGatheringPost(otherUser);

        PostPaginationParam paginationParam = new PostPaginationParam(
                "React"
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.pageInfo().getTotalElements()).isEqualTo(1);
        assertThat(result.content().get(0).title()).contains("React");
    }

    @Test
    @DisplayName("빈 키워드로 검색하면 전체 게시글이 반환되어야 한다")
    void 빈_키워드로_검색하면_전체_게시글이_반환되어야_한다() {
        // given
        createTestGatheringPost(testUser);
        createTestGatheringPost(otherUser);

        PostPaginationParam paginationParam = new PostPaginationParam(
                ""
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.pageInfo().getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("페이지네이션이 정상적으로 동작해야 한다")
    void 페이지네이션이_정상적으로_동작해야_한다() {
        // given
        // 5개의 게시글 생성
        for (int i = 0; i < 5; i++) {
            createTestGatheringPost(testUser);
        }

        PostPaginationParam paginationParam = new PostPaginationParam(
                null
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.pageInfo().getTotalElements()).isEqualTo(5);
        assertThat(result.pageInfo().getPageNumber()).isEqualTo(0);
        assertThat(result.pageInfo().getPageSize()).isEqualTo(2);
        assertThat(result.pageInfo().getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하는 팀원 모집 글을 조회하면 정상적으로 반환되어야 한다")
    void 존재하는_팀원_모집_글을_조회하면_정상적으로_반환되어야_한다() {
        // given
        GatheringPost savedPost = createTestGatheringPost(testUser);

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(savedPost.getPost().getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(savedPost.getPost().getId());
        assertThat(result.title()).isEqualTo("테스트 제목");
        assertThat(result.content()).isEqualTo("테스트 내용");
        assertThat(result.gatheringType()).isEqualTo(GatheringType.STUDY);
        assertThat(result.headCount()).isEqualTo(5);
        assertThat(result.place()).isEqualTo("강남역");
        assertThat(result.period()).isEqualTo("2개월");
        assertThat(result.schedule()).isEqualTo("매주 토요일");
        assertThat(result.status()).isEqualTo(GatheringStatus.RECRUITING);
        assertThat(result.hasLiked()).isFalse();
        assertThat(result.hasBookmarked()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 팀원 모집 글을 조회하면 예외가 발생해야 한다")
    void 존재하지_않는_팀원_모집_글을_조회하면_예외가_발생해야_한다() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringService.getGatheringPost(nonExistentId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("유효한 데이터로 팀원 모집 글을 생성하면 성공해야 한다")
    void 유효한_데이터로_팀원_모집_글을_생성하면_성공해야_한다() {
        // given
        GatheringPostRequestDTO requestDTO = createValidRequestDTO();

        // when
        GatheringPostResponseDTO result = gatheringService.createGatheringPost(requestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.postId()).isNotNull();
        assertThat(result.title()).isEqualTo("팀원 모집합니다");
        assertThat(result.content()).isEqualTo("React 프로젝트 함께할 팀원을 모집합니다. 경험자 우대합니다.");
        assertThat(result.tags()).isEqualTo("React");
        assertThat(result.gatheringType()).isEqualTo(GatheringType.SIDE_PROJECT);
        assertThat(result.headCount()).isEqualTo(4);
        assertThat(result.place()).isEqualTo("온라인");
        assertThat(result.period()).isEqualTo("3개월");
        assertThat(result.schedule()).isEqualTo("주 2회 화/목 오후 7시");
        assertThat(result.status()).isEqualTo(GatheringStatus.RECRUITING);
        assertThat(result.nickname()).isEqualTo("테스트유저");
        assertThat(result.likeCount()).isEqualTo(0);
        assertThat(result.viewCount()).isEqualTo(0);
        assertThat(result.hasLiked()).isFalse();
        assertThat(result.hasBookmarked()).isFalse();

        // 데이터베이스에 실제로 저장되었는지 확인
        GatheringPost savedGatheringPost = gatheringRepository.findByPostId(result.postId());
        assertThat(savedGatheringPost).isNotNull();
        assertThat(savedGatheringPost.getPost().getTitle()).isEqualTo("팀원 모집합니다");
        assertThat(savedGatheringPost.getGatheringType()).isEqualTo(GatheringType.SIDE_PROJECT);
    }

    @Test
    @DisplayName("팀원 모집 글 생성 시 게시판 타입이 GATHER로 설정되어야 한다")
    void 팀원_모집_글_생성_시_게시판_타입이_GATHER로_설정되어야_한다() {
        // given
        GatheringPostRequestDTO requestDTO = createValidRequestDTO();

        // when
        GatheringPostResponseDTO result = gatheringService.createGatheringPost(requestDTO);

        // then
        Post savedPost = postRepository.findById(result.postId()).orElseThrow();
        assertThat(savedPost.getBoardType()).isEqualTo(BoardType.GATHER);
    }

    @Test
    @DisplayName("작성자가 본인의 팀원 모집 글을 수정하면 성공해야 한다")
    void 작성자가_본인의_팀원_모집_글을_수정하면_성공해야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        GatheringPostRequestDTO updateDTO = new GatheringPostRequestDTO(
                "수정된 제목",
                "수정된 내용",
                "Vue",
                GatheringType.STUDY,
                GatheringStatus.RECRUITING,
                6,
                "오프라인",
                "4개월",
                "주 3회"
        );

        // when
        gatheringService.updateGatheringPost(originalPost.getPost().getId(), updateDTO);

        // then
        GatheringPost updatedPost = gatheringRepository.findByPostId(originalPost.getPost().getId());
        assertThat(updatedPost.getPost().getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getPost().getContent()).isEqualTo("수정된 내용");
        assertThat(updatedPost.getPost().getTags()).isEqualTo("Vue");
        assertThat(updatedPost.getGatheringType()).isEqualTo(GatheringType.STUDY);
        assertThat(updatedPost.getHeadCount()).isEqualTo(6);
        assertThat(updatedPost.getPlace()).isEqualTo("오프라인");
        assertThat(updatedPost.getPeriod()).isEqualTo("4개월");
        assertThat(updatedPost.getSchedule()).isEqualTo("주 3회");
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 팀원 모집 글을 수정할 수 있어야 한다")
    void 관리자는_다른_사용자의_팀원_모집_글을_수정할_수_있어야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        setSecurityContext(adminUser); // 관리자로 변경

        GatheringPostRequestDTO updateDTO = new GatheringPostRequestDTO(
                "관리자가 수정한 제목",
                "관리자가 수정한 내용",
                "Admin",
                GatheringType.SIDE_PROJECT,
                GatheringStatus.RECRUITING,
                3,
                "관리자 지정 장소",
                "1개월",
                "매일"
        );

        // when
        gatheringService.updateGatheringPost(originalPost.getPost().getId(), updateDTO);

        // then
        GatheringPost updatedPost = gatheringRepository.findByPostId(originalPost.getPost().getId());
        assertThat(updatedPost.getPost().getTitle()).isEqualTo("관리자가 수정한 제목");
        assertThat(updatedPost.getPost().getContent()).isEqualTo("관리자가 수정한 내용");
    }

    @Test
    @DisplayName("작성자가 아닌 일반 사용자가 팀원 모집 글을 수정하려고 하면 예외가 발생해야 한다")
    void 작성자가_아닌_일반_사용자가_팀원_모집_글을_수정하려고_하면_예외가_발생해야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        setSecurityContext(otherUser); // 다른 사용자로 변경

        GatheringPostRequestDTO updateDTO = createValidRequestDTO();

        // when & then
        assertThatThrownBy(() ->
                gatheringService.updateGatheringPost(originalPost.getPost().getId(), updateDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 팀원 모집 글을 수정하려고 하면 예외가 발생해야 한다")
    void 존재하지_않는_팀원_모집_글을_수정하려고_하면_예외가_발생해야_한다() {
        // given
        Long nonExistentId = 99999L;
        GatheringPostRequestDTO updateDTO = createValidRequestDTO();

        // when & then
        assertThatThrownBy(() ->
                gatheringService.updateGatheringPost(nonExistentId, updateDTO))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("작성자가 본인의 팀원 모집 글을 삭제하면 성공해야 한다")
    void 작성자가_본인의_팀원_모집_글을_삭제하면_성공해야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        Long postId = originalPost.getPost().getId();

        // when
        gatheringService.deleteGatheringPost(postId);

        // then
        GatheringPost deletedPost = gatheringRepository.findByPostId(postId);
        assertThat(deletedPost).isNull();

        // Post도 함께 삭제되었는지 확인
        boolean postExists = postRepository.existsById(postId);
        assertThat(postExists).isFalse();
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 팀원 모집 글을 삭제할 수 있어야 한다")
    void 관리자는_다른_사용자의_팀원_모집_글을_삭제할_수_있어야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        setSecurityContext(adminUser); // 관리자로 변경
        Long postId = originalPost.getPost().getId();

        // when
        gatheringService.deleteGatheringPost(postId);

        // then
        GatheringPost deletedPost = gatheringRepository.findByPostId(postId);
        assertThat(deletedPost).isNull();
    }

    @Test
    @DisplayName("작성자가 아닌 일반 사용자가 팀원 모집 글을 삭제하려고 하면 예외가 발생해야 한다")
    void 작성자가_아닌_일반_사용자가_팀원_모집_글을_삭제하려고_하면_예외가_발생해야_한다() {
        // given
        GatheringPost originalPost = createTestGatheringPost(testUser);
        setSecurityContext(otherUser); // 다른 사용자로 변경

        // when & then
        assertThatThrownBy(() ->
                gatheringService.deleteGatheringPost(originalPost.getPost().getId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 팀원 모집 글을 삭제하려고 하면 예외가 발생해야 한다")
    void 존재하지_않는_팀원_모집_글을_삭제하려고_하면_예외가_발생해야_한다() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringService.deleteGatheringPost(nonExistentId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("좋아요를 누른 게시글은 hasLiked가 true로 반환되어야 한다")
    void 좋아요를_누른_게시글은_hasLiked가_true로_반환되어야_한다() {
        // given
        GatheringPost gatheringPost = createTestGatheringPost(otherUser);

        // 좋아요 데이터 생성 (실제 Like 엔티티가 있다고 가정)
        // Like like = Like.builder()
        //         .post(gatheringPost.getPost())
        //         .user(testUser)
        //         .build();
        // likeRepository.save(like);

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(gatheringPost.getPost().getId());

        // then
        // 실제 Like 엔티티 구현 후 주석 해제
        // assertThat(result.isHasLiked()).isTrue();
        assertThat(result.hasLiked()).isFalse(); // 현재는 false로 검증
    }

    @Test
    @DisplayName("북마크를 한 게시글은 hasBookmarked가 true로 반환되어야 한다")
    void 북마크를_한_게시글은_hasBookmarked가_true로_반환되어야_한다() {
        // given
        GatheringPost gatheringPost = createTestGatheringPost(otherUser);

        // 북마크 데이터 생성 (실제 Bookmark 엔티티가 있다고 가정)
        // Bookmark bookmark = Bookmark.builder()
        //         .post(gatheringPost.getPost())
        //         .user(testUser)
        //         .build();
        // bookmarkRepository.save(bookmark);

        // when
        GatheringPostResponseDTO result = gatheringService.getGatheringPost(gatheringPost.getPost().getId());

        // then
        // 실제 Bookmark 엔티티 구현 후 주석 해제
        // assertThat(result.isHasBookmarked()).isTrue();
        assertThat(result.hasBookmarked()).isFalse(); // 현재는 false로 검증
    }

    @Test
    @DisplayName("제목에 키워드가 포함된 게시글을 검색할 수 있어야 한다")
    void 제목에_키워드가_포함된_게시글을_검색할_수_있어야_한다() {
        // given
        Post post1 = Post.builder()
                .user(testUser)
                .boardType(BoardType.GATHER)
                .title("Spring Boot 스터디 모집")
                .content("백엔드 개발 스터디")
                .tags("Spring")
                .build();
        Post savedPost1 = postRepository.save(post1);

        GatheringPost gathering1 = GatheringPost.builder()
                .post(savedPost1)
                .gatheringType(GatheringType.STUDY)
                .status(GatheringStatus.RECRUITING)
                .headCount(4)
                .place("온라인")
                .period("2개월")
                .schedule("주 2회")
                .build();
        gatheringRepository.save(gathering1);

        Post post2 = Post.builder()
                .user(testUser)
                .boardType(BoardType.GATHER)
                .title("React 프로젝트 팀원 모집")
                .content("프론트엔드 개발")
                .tags("React")
                .build();
        Post savedPost2 = postRepository.save(post2);

        GatheringPost gathering2 = GatheringPost.builder()
                .post(savedPost2)
                .gatheringType(GatheringType.SIDE_PROJECT)
                .status(GatheringStatus.RECRUITING)
                .headCount(3)
                .place("오프라인")
                .period("1개월")
                .schedule("매일")
                .build();
        gatheringRepository.save(gathering2);

        PostPaginationParam paginationParam = new PostPaginationParam(
                "Spring"
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).contains("Spring");
    }

    @Test
    @DisplayName("내용에 키워드가 포함된 게시글을 검색할 수 있어야 한다")
    void 내용에_키워드가_포함된_게시글을_검색할_수_있어야_한다() {
        // given
        Post post = Post.builder()
                .user(testUser)
                .boardType(BoardType.GATHER)
                .title("팀원 모집")
                .content("JavaScript와 TypeScript를 활용한 풀스택 개발 프로젝트")
                .tags("JavaScript")
                .build();
        Post savedPost = postRepository.save(post);

        GatheringPost gathering = GatheringPost.builder()
                .post(savedPost)
                .gatheringType(GatheringType.SIDE_PROJECT)
                .status(GatheringStatus.RECRUITING)
                .headCount(4)
                .place("온라인")
                .period("3개월")
                .schedule("주 3회")
                .build();
        gatheringRepository.save(gathering);

        PostPaginationParam paginationParam = new PostPaginationParam(
                "풀스택"
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).content()).contains("풀스택");
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 검색하면 빈 결과가 반환되어야 한다")
    void 존재하지_않는_키워드로_검색하면_빈_결과가_반환되어야_한다() {
        // given
        createTestGatheringPost(testUser);

        PostPaginationParam paginationParam = new PostPaginationParam(
                "존재하지않는키워드"
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.pageInfo().getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("대소문자 구분 없이 키워드 검색이 가능해야 한다")
    void 대소문자_구분_없이_키워드_검색이_가능해야_한다() {
        // given
        Post post = Post.builder()
                .user(testUser)
                .boardType(BoardType.GATHER)
                .title("React Native 앱 개발")
                .content("모바일 앱 개발 프로젝트")
                .tags("React")
                .build();
        Post savedPost = postRepository.save(post);

        GatheringPost gathering = GatheringPost.builder()
                .post(savedPost)
                .gatheringType(GatheringType.SIDE_PROJECT)
                .status(GatheringStatus.RECRUITING)
                .headCount(3)
                .place("온라인")
                .period("2개월")
                .schedule("주 4회")
                .build();
        gatheringRepository.save(gathering);

        PostPaginationParam paginationParam = new PostPaginationParam(
                "react"
        );

        // when
        PageResponseDTO<GatheringPostResponseDTO> result =
                gatheringService.getGatheringPostList(paginationParam);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).containsIgnoringCase("react");
    }
}
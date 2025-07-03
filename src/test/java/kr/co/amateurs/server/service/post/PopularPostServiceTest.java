package kr.co.amateurs.server.service.post;

import kr.co.amateurs.server.domain.dto.post.PopularPostRequest;
import kr.co.amateurs.server.domain.dto.post.PopularPostResponse;
import kr.co.amateurs.server.repository.post.PopularPostRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static kr.co.amateurs.server.fixture.post.PopularPostRequestFixture.생성;

import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PopularPostServiceTest {

    @Autowired
    PopularPostService popularPostService;

    @MockitoBean
    PopularPostRepository popularPostRepository;

    @MockitoBean
    PostService postService;

    @Nested
    class 인기글_계산_및_저장_테스트 {
        @Test
        void 게시글_없으면_저장_호출_안함() {
            // given
            when(popularPostRepository.findRecentPostsWithCounts(any())).thenReturn(List.of());

            // when
            popularPostService.calculateAndSavePopularPosts();

            // then
            verify(popularPostRepository, never()).savePopularPosts(any());
            verify(popularPostRepository, never()).deleteBeforeDate(any());
        }

        @Test
        void 게시글_있으면_점수계산_저장_삭제_호출() {
            // given
            List<PopularPostRequest> posts = List.of(
                    생성(1L, 100, 10, 5, 0.0, LocalDate.now()),
                    생성(2L, 200, 20, 10, 0.0, LocalDate.now())
            );
            when(popularPostRepository.findRecentPostsWithCounts(any())).thenReturn(posts);

            // when
            popularPostService.calculateAndSavePopularPosts();

            // then
            verify(popularPostRepository).savePopularPosts(any());
            verify(popularPostRepository).deleteBeforeDate(any());
        }

        @Test
        void 인기점수_정렬_상위10개만_저장() {
            // given
            List<PopularPostRequest> posts =
                    java.util.stream.IntStream.rangeClosed(1, 20)
                            .mapToObj(i -> 생성((long) i, 100 * i, 10 * i, 5 * i, 0.0, LocalDate.now()))
                            .toList();
            when(popularPostRepository.findRecentPostsWithCounts(any())).thenReturn(posts);

            ArgumentCaptor<List<PopularPostRequest>> captor = ArgumentCaptor.forClass(List.class);

            // when
            popularPostService.calculateAndSavePopularPosts();

            // then
            verify(popularPostRepository).savePopularPosts(captor.capture());
            List<PopularPostRequest> saved = captor.getValue();
            assertThat(saved).hasSize(10);
            // 점수 내림차순 정렬 확인
            for (int i = 0; i < saved.size() - 1; i++) {
                assertThat(saved.get(i).popularityScore()).isGreaterThanOrEqualTo(saved.get(i + 1).popularityScore());
            }
        }
    }

    @Nested
    class 인기글_조회_테스트 {
        @Test
        void 인기글_조회_정상() {
            // given
            List<PopularPostRequest> posts = List.of(
                    생성(1L, 100, 10, 5, 123.4, LocalDate.now())
            );
            when(popularPostRepository.findLatestPopularPosts(anyInt())).thenReturn(posts);

            // when
            List<PopularPostResponse> result = popularPostService.getPopularPosts(10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }

        @Test
        void 인기글_없으면_빈리스트() {
            // given
            when(popularPostRepository.findLatestPopularPosts(anyInt())).thenReturn(List.of());

            // when
            List<PopularPostResponse> result = popularPostService.getPopularPosts(10);

            // then
            assertThat(result).isEmpty();
        }
    }
} 
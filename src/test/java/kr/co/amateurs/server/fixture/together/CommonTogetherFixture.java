package kr.co.amateurs.server.fixture.together;

import kr.co.amateurs.server.config.jwt.JwtProvider;
import kr.co.amateurs.server.domain.dto.common.PaginationSortType;
import kr.co.amateurs.server.domain.dto.common.PostPaginationParam;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import kr.co.amateurs.server.domain.entity.user.User;
import kr.co.amateurs.server.domain.entity.user.enums.Role;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class CommonTogetherFixture {
    private static int seq = 0;
    public static Post createPost(User user, String title, String content, String tags, BoardType boardType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .tags(tags)
                .viewCount(0)
                .likeCount(0)
                .isDeleted(false)
                .boardType(boardType)
                .build();
    }

    public static User createStudent() {
        seq = seq + 1;
        return User.builder()
                .role(Role.STUDENT)
                .devcourseName(DevCourseTrack.AI_BACKEND)
                .devcourseBatch("1기")
                .email("student"+ seq + "@test.com")
                .name("student_name"+ seq)
                .nickname("student_nickname"+ seq)
                .build();
    }

    public static User createAdmin() {
        seq = seq + 1;
        return User.builder()
                .role(Role.ADMIN)
                .email("admin"+seq+"@test.com")
                .name("admin_name"+ seq)
                .nickname("admin_nickname"+ seq)
                .build();
    }

    public static User createGuest() {
        seq = seq + 1;
        return User.builder()
                .role(Role.GUEST)
                .email("guest"+seq+"@test.com")
                .name("guest_name"+seq)
                .nickname("guest_nickname"+seq)
                .build();
    }

    public static PostPaginationParam createPaginationParam(String keyword, PaginationSortType sortType) {
        return PostPaginationParam.builder()
                .keyword(keyword)
                .page(0)
                .size(10)
                .field(sortType)
                .sortDirection(Sort.Direction.DESC)
                .build();
    }

    public static List<String> makeTag(String tag){
        List<String> tags = new ArrayList<String>();
        tags.add(tag);
        return tags;
    }
}

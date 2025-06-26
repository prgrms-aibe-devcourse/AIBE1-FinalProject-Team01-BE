package kr.co.amateurs.server.repository.project;

import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
                select p
                from Project p
                join fetch p.post post
                join fetch p.post.user user
                where (:keyword is null or :keyword = ''
                       or post.title like concat('%', :keyword, '%')
                       or post.content like concat('%', :keyword, '%'))
                  and (:course is null or p.post.user.devcourseName = :course)
                  and (:batch is null or p.post.user.devcourseBatch = :batch)
                order by post.likeCount desc
            """)
    Page<Project> findAllByFilterOptionsOrderByLikeCountDesc(
            @Param("keyword") String search,
            @Param("course") DevCourseTrack course,
            @Param("batch") String batch,
            Pageable pageable);
}

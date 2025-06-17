package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.entity.post.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}

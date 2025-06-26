package kr.co.amateurs.server.fixture.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.Project;
import kr.co.amateurs.server.domain.entity.post.enums.DevCourseTrack;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectFixture {
    public static Project createBackendProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("백엔드 프로젝트")
                .githubUrl("https://github.com/example/backend-project")
                .demoUrl("https://example.com/demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }

    public static Project createFrontendProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("프론트엔드 프로젝트")
                .githubUrl("https://github.com/example/frontend-project")
                .demoUrl("https://example.com/demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }

    public static Project createAiBackendProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("AI 백엔드 프로젝트")
                .githubUrl("https://github.com/example/ai-backend-project")
                .demoUrl("https://example.com/ai-demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }

    public static Project createFullStackProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("풀스택 프로젝트")
                .githubUrl("https://github.com/example/fullstack-project")
                .demoUrl("https://example.com/fullstack-demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }

    public static Project createDataScienceProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("데이터 분석 프로젝트")
                .githubUrl("https://github.com/example/data-science-project")
                .demoUrl("https://example.com/data-analysis-demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }

    public static Project createDataEngineeringProject(Post post, LocalDateTime startedAt, LocalDateTime endedAt, List<String> projectMembers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return Project.builder()
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .simpleContent("데이터 엔지니어링 프로젝트")
                .githubUrl("https://github.com/example/data-engineering-project")
                .demoUrl("https://example.com/data-pipeline-demo")
                .projectMembers(objectMapper.writeValueAsString(projectMembers))
                .build();
    }
}

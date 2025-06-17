package kr.co.amateurs.server.service.post;

public interface BasePostService {
    String createPost();

    String readPost();

    void readPosts();

    void updatePost();

    void deletePost();
}

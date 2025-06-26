package kr.co.amateurs.server.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;

@TestConfiguration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;
    private int port;

    @PostConstruct
    public void startRedis() throws IOException {
        port = findAvailablePort();
        redisServer = new RedisServer(port);
        redisServer.start();
        System.out.println("Embedded Redis started on port: " + port);
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            try {
                redisServer.stop();
                System.out.println("Embedded Redis stopped");
            } catch (IOException e) {
                System.err.println("Redis 종료 중 오류: " + e.getMessage());
            }
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", port);
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
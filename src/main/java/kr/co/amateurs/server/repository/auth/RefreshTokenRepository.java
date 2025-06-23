package kr.co.amateurs.server.repository.auth;

import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByEmail(String email);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);
}

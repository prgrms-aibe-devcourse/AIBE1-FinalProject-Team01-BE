package kr.co.amateurs.server.repository.auth;

import kr.co.amateurs.server.domain.entity.auth.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}

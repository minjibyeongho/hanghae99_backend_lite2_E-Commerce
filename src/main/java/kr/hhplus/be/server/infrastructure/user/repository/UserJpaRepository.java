package kr.hhplus.be.server.infrastructure.user.repository;

import kr.hhplus.be.server.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
}

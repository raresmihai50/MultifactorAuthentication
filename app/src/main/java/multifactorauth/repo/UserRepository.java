package multifactorauth.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import multifactorauth.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Boot "citește" numele metodei și generează SQL-ul: SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
}
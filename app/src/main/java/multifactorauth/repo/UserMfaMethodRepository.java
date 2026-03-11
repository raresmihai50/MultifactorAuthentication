package multifactorauth.repo;

import multifactorauth.domain.User;
import multifactorauth.domain.UserMfaMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMfaMethodRepository extends JpaRepository<UserMfaMethod, Long> {
    
    // Ne aduce toate metodele MFA active ale unui anumit user:
    // SELECT * FROM user_mfa_methods WHERE user_id = ? AND is_enabled = true
    List<UserMfaMethod> findByUserAndIsEnabledTrue(User user);

    // Găsește o metodă specifică a unui user (ex: caută metoda "TOTP" a lui Gigel)
    Optional<UserMfaMethod> findByUserAndProviderName(User user, String providerName);
}
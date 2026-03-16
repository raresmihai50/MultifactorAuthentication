package multifactorauth.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // One-to-Many. 
    // "cascade = CascadeType.ALL" dacă ștergi userul, se șterg automat și metodele lui MFA din baza de date.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMfaMethod> mfaMethods = new ArrayList<>();

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // --- Getters și Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<UserMfaMethod> getMfaMethods() { return mfaMethods; }
    public void setMfaMethods(List<UserMfaMethod> mfaMethods) { this.mfaMethods = mfaMethods; }
}
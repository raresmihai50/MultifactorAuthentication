package multifactorauth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_mfa_methods")
public class UserMfaMethod extends BaseEntity {

    // Relația Many-to-One: Multe metode pot aparține unui singur User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Numele metodei: "TOTP", "EMAIL", etc. (daca va fi cazul, putem adăuga și alte tipuri de metode MFA în viitor)
    @Column(name = "provider_name", nullable = false)
    private String providerName; 

    // Aici salvăm cheia secretă DOAR dacă metoda o cere (ex: pentru TOTP). Pentru Email poate fi null.
    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    public UserMfaMethod() {}

    public UserMfaMethod(User user, String providerName, String secretKey) {
        this.user = user;
        this.providerName = providerName;
        this.secretKey = secretKey;
    }

    // --- Getters și Setters ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}
package multifactorauth.services;

import multifactorauth.domain.User;

public interface MfaProvider {
    // Returnează numele ("TOTP", "EMAIL")
    String getProviderName(); 
    
    // Trimite provocarea (ex: trimite mail-ul cu codul)
    void sendChallenge(User user); 
    
    // Validează codul introdus de user
    boolean verifyCode(User user, String code); 
}
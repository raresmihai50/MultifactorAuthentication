package multifactorauth.services;

import multifactorauth.domain.User;
import multifactorauth.dto.MfaChallengeResponse;

public interface MfaProvider {
    String getProviderName();
    
    // Se returneaza un obiect care conține toate informațiile necesare pentru a afișa provocarea MFA (ex: mesaj text, imagine QR, etc.)
    MfaChallengeResponse generateChallenge(User user); 
    
    boolean verifyCode(User user, String code);
}
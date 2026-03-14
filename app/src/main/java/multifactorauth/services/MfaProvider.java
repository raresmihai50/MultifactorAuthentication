package multifactorauth.services;

import multifactorauth.domain.User;
import multifactorauth.dto.MfaChallengeResponse; // Nu uita importul!

public interface MfaProvider {
    String getProviderName();
    
    // Acum returnăm un obiect clar și predictibil!
    MfaChallengeResponse generateChallenge(User user); 
    
    boolean verifyCode(User user, String code);
}
package multifactorauth.services;

import multifactorauth.domain.User;
import org.springframework.stereotype.Service;

@Service
public class EmailMfaProvider implements MfaProvider {

    @Override
    public String getProviderName() {
        return "EMAIL";
    }

    @Override
    public void sendChallenge(User user) {
        // TODO: Generăm 6 cifre random, le salvăm temporar și trimitem un email
    }

    @Override
    public boolean verifyCode(User user, String code) {
        // TODO: Verificăm dacă codul primit pe mail e corect și nu a expirat
        return false;
    }
}
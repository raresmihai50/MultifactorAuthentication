package multifactorauth.services;

import multifactorauth.domain.User;
import org.springframework.stereotype.Service;

@Service
public class TotpMfaProvider implements MfaProvider {

    @Override
    public String getProviderName() {
        return "TOTP";
    }

    @Override
    public void sendChallenge(User user) {
        // TODO: Pentru TOTP nu trimitem nimic, codul se generează pe telefonul userului.
    }

    @Override
    public boolean verifyCode(User user, String code) {
        // TODO: Aici vom lua secret_key din baza de date și vom rula algoritmul de validare
        return false; 
    }
}
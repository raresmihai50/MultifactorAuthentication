package multifactorauth.dto;

import java.util.List;

public class LoginResponse {
    
    private boolean mfaRequired;
    private List<String> availableMfaMethods;

    // Aici ar veni token ul JWT dar pentru simplitate, o să ne limităm la un mesaj text
    private String message; 

    public LoginResponse(boolean mfaRequired, List<String> availableMfaMethods, String message) {
        this.mfaRequired = mfaRequired;
        this.availableMfaMethods = availableMfaMethods;
        this.message = message;
    }

    // --- Getters ---
    public boolean isMfaRequired() { return mfaRequired; }
    public List<String> getAvailableMfaMethods() { return availableMfaMethods; }
    public String getMessage() { return message; }
}
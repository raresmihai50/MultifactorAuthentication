package multifactorauth.dto;

public class MfaChallengeResponse {
    private String type;       // Va fi "TEXT" sau "QR"
    private String message;    // Mesajul text pentru utilizator
    private String qrCodeData; // Imaginea Base64

    public MfaChallengeResponse(String type, String message, String qrCodeData) {
        this.type = type;
        this.message = message;
        this.qrCodeData = qrCodeData;
    }

    // --- Getters ---
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getQrCodeData() { return qrCodeData; }
}
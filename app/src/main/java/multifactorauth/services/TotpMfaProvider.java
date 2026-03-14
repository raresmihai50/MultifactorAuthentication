package multifactorauth.services;

import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator; // Importăm DTO-ul!
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.util.Utils;
import multifactorauth.domain.User;
import multifactorauth.domain.UserMfaMethod;
import multifactorauth.dto.MfaChallengeResponse;
import multifactorauth.repo.UserMfaMethodRepository;

@Service
public class TotpMfaProvider implements MfaProvider {

    private final UserMfaMethodRepository mfaMethodRepository;

    public TotpMfaProvider(UserMfaMethodRepository mfaMethodRepository) {
        this.mfaMethodRepository = mfaMethodRepository;
    }

    @Override
    public String getProviderName() {
        return "TOTP";
    }

    // AICI E MODIFICAREA: Folosim MfaChallengeResponse
    @Override
    public MfaChallengeResponse generateChallenge(User user) {
        // 1. Generăm un Secret Key unic lung pentru acest user
        String secret = new DefaultSecretGenerator().generate();

        // 2. Îl salvăm în baza de date
        UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, "TOTP")
                .orElseThrow(() -> new RuntimeException("Metoda TOTP nu a fost găsită!"));
        method.setSecretKey(secret);
        mfaMethodRepository.save(method);

        // 3. Creăm datele pentru codul QR (ceea ce va scana Google Authenticator)
        QrData data = new QrData.Builder()
                .label(user.getEmail()) // Va apărea în aplicația de pe telefon
                .secret(secret)
                .issuer("MFA Project") // Numele aplicației tale
                .algorithm(HashingAlgorithm.SHA1) // Standardul Google Auth
                .digits(6)
                .period(30)
                .build();

        try {
            // 4. Transformăm datele într-o imagine Base64
            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            String qrBase64Url = Utils.getDataUriForImage(imageData, mimeType);

            // 5. Trimitem DTO-ul complet către React, care conține imaginea!
            return new MfaChallengeResponse(
                "QR", 
                "Scanează codul de mai jos în Google Authenticator!", 
                qrBase64Url
            );

        } catch (QrGenerationException e) {
            throw new RuntimeException("Eroare la generarea codului QR!");
        }
    }

    @Override
    public boolean verifyCode(User user, String code) {
        UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, "TOTP")
                .orElseThrow(() -> new RuntimeException("Metoda TOTP nu a fost găsită!"));

        String secret = method.getSecretKey();
        if (secret == null || secret.isEmpty()) return false;

        // Verificăm dacă codul tastat corespunde cu timpul curent și secretul
        CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        
        return verifier.isValidCode(secret, code);
    }
}
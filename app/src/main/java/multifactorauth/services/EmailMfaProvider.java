package multifactorauth.services;

import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import multifactorauth.domain.User;
import multifactorauth.domain.UserMfaMethod;
import multifactorauth.dto.MfaChallengeResponse;
import multifactorauth.repo.UserMfaMethodRepository;

@Service
public class EmailMfaProvider implements MfaProvider {

    private final UserMfaMethodRepository mfaMethodRepository;
    private final JavaMailSender mailSender;
    public EmailMfaProvider(UserMfaMethodRepository mfaMethodRepository, JavaMailSender mailSender) {
        this.mfaMethodRepository = mfaMethodRepository;
        this.mailSender = mailSender;
    }

    @Override
    public String getProviderName() {
        return "EMAIL";
    }

    @Override
    public MfaChallengeResponse generateChallenge(User user) {
        // 1. Generăm codul de 6 cifre
        String code = String.format("%06d", new Random().nextInt(999999));

        // 2. Îl salvăm în baza de date
        UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, "EMAIL")
                .orElseThrow(() -> new RuntimeException("Metoda EMAIL nu a fost găsită pentru acest user!"));

        method.setSecretKey(code);
        mfaMethodRepository.save(method);

        // 3. Creăm email-ul real
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail()); 
        message.setSubject("🔒 Codul tău de securitate MFA");
        message.setText("Salut " + user.getUsername() + ",\n\n"
                + "Ai solicitat un cod de verificare pentru contul tău.\n"
                + "Codul tău de securitate este: " + code + "\n\n"
                + "Dacă nu ai solicitat tu acest cod, te rugăm să ignori acest mesaj.");

        // 4. Trimitem email-ul!
        System.out.println("Se trimite email către " + user.getEmail() + "...");
        mailSender.send(message);
        System.out.println("Email-ul a fost trimis cu succes!");

        // 5. Returnăm DTO-ul ca să știe React-ul ce să afișeze
        return new MfaChallengeResponse(
            "TEXT", 
            "Codul a fost trimis pe email-ul tău. Te rugăm să-l introduci pentru confirmare!", 
            null // QR-ul este null pentru că la email nu avem imagine
        );
    }

    @Override
    public boolean verifyCode(User user, String code) {
        UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, "EMAIL")
                .orElseThrow(() -> new RuntimeException("Metoda EMAIL nu a fost găsită!"));

        if (method.getSecretKey() != null && method.getSecretKey().equals(code)) {
            method.setSecretKey(null); // Ștergem codul după folosire ca să nu poată fi refolosit
            mfaMethodRepository.save(method);
            return true;
        }
        return false;
    }
}
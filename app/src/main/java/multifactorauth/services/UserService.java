package multifactorauth.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import multifactorauth.domain.User;
import multifactorauth.domain.UserMfaMethod;
import multifactorauth.dto.LoginResponse;
import multifactorauth.repo.UserMfaMethodRepository;
import multifactorauth.repo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMfaMethodRepository mfaMethodRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Aici e magia Strategy Pattern: Spring ne va injecta automat TOATE clasele 
    // care implementează MfaProvider (și TotpMfaProvider, și EmailMfaProvider)
    private final List<MfaProvider> mfaProviders;

    public UserService(UserRepository userRepository, 
                       UserMfaMethodRepository mfaMethodRepository, 
                       List<MfaProvider> mfaProviders,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mfaMethodRepository = mfaMethodRepository;
        this.mfaProviders = mfaProviders;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. ÎNREGISTRAREA ---
    public User registerUser(String username, String email, String rawPassword, List<String> selectedMfas) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Eroare: Acest username este deja folosit!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Eroare: Acest email este deja folosit!");
        }

        // Criptăm parola
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Creăm userul
        User newUser = new User(username, email, encodedPassword);
        User savedUser = userRepository.save(newUser);
        
        // Dacă a bifat metode MFA în React, le salvăm în baza de date
        if (selectedMfas != null && !selectedMfas.isEmpty()) {
            for (String mfaName : selectedMfas) {
                // Le setăm inițial cu isEnabled = false, secretKey = null
                UserMfaMethod method = new UserMfaMethod(savedUser, mfaName, null);
                method.setEnabled(false); // Vor deveni true doar după ce le configurează!
                mfaMethodRepository.save(method);
            }
        }

        System.out.println("User înregistrat cu succes: " + username + " cu metodele: " + selectedMfas);
        return savedUser;
    }

    // --- 2. LOGIN PASUL 1 (Verificarea Parolei și a Metodelor MFA) ---
    public LoginResponse loginStep1(String email, String rawPassword) { // Returnăm LoginResponse, nu Object
        
        // 1. Căutăm userul în baza de date
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Eroare: Nu există niciun cont cu acest email!"));

        // 2. Comparăm parola introdusă cu HASH-ul
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Eroare: Parolă incorectă!");
        }

        // 3. Căutăm ce metode MFA are activate
        List<UserMfaMethod> activeMethods = mfaMethodRepository.findByUserAndIsEnabledTrue(user);

        List<String> availableMfaMethods = activeMethods.stream()
                .map(UserMfaMethod::getProviderName)
                .collect(Collectors.toList());

        // 4. Returnăm un răspuns CLAR și TIPIZAT
        if (availableMfaMethods.isEmpty()) {
            System.out.println("-> Login Step 1: Userul nu are MFA. Intră direct în cont!");
            // mfaRequired = false, methods = null, message = "LOGIN_SUCCESS"
            return new LoginResponse(false, null, "LOGIN_SUCCESS"); 
        } else {
            System.out.println("-> Login Step 1: Parolă corectă! Se cer metodele MFA: " + availableMfaMethods);
            // mfaRequired = true, methods = lista noastră, message = "MFA_REQUIRED"
            return new LoginResponse(true, availableMfaMethods, "MFA_REQUIRED"); 
        }
    }
    // --- 3. CONFIGURARE MFA (Trimiterea codului inițial) ---
    public Object setupMfaMethod(String email, String providerName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        // Verificăm dacă metoda MFA există deja pentru el în DB. Dacă nu, o creăm!
        Optional<UserMfaMethod> existingMethod = mfaMethodRepository.findByUserAndProviderName(user, providerName);
        if (existingMethod.isEmpty()) {
            UserMfaMethod newMethod = new UserMfaMethod(user, providerName, null);
            newMethod.setEnabled(false); // Încă nu e validată!
            mfaMethodRepository.save(newMethod);
        }

        // Căutăm providerul (ex: EmailMfaProvider)
        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        // Trimitem email-ul cu codul
        if (providerName.equals("EMAIL")) {
            provider.sendChallenge(user);
            return "Codul a fost trimis pe email-ul tău. Te rugăm să-l introduci pentru confirmare!";
        }
        
        return null;
    }

    // --- 4. CONFIRMARE MFA (Validarea codului și activarea metodei) ---
    public boolean confirmMfaSetup(String email, String providerName, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        // Apelează 'verifyCode' din EmailMfaProvider
        if (provider.verifyCode(user, code)) {
            // Dacă codul e bun, căutăm metoda în DB și îi punem is_enabled = true
            UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, providerName)
                    .orElseThrow(() -> new RuntimeException("Metoda nu există pentru acest user!"));
            
            method.setEnabled(true); // <--- AICI SE FACE MAGIA
            mfaMethodRepository.save(method);
            System.out.println("Metoda " + providerName + " a fost ACTIVATĂ cu succes pentru " + email);
            return true;
        }

        throw new RuntimeException("Codul introdus este incorect!");
    }

    /// --- Trimite codul atunci când userul încearcă să se logheze ---
    public void sendLoginChallenge(String email, String providerName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        provider.sendChallenge(user);
    }

    // --- 5. LOGIN PASUL 2 (Verificarea codului propriu-zis la logare) ---
    public String loginStep2(String email, String providerName, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        // Verificăm dacă metoda este activă pe bune (is_enabled = true)
        UserMfaMethod method = mfaMethodRepository.findByUserAndProviderName(user, providerName)
                .orElseThrow(() -> new RuntimeException("Metoda MFA nu există pentru acest user!"));
        
        if (!method.isEnabled()) {
            throw new RuntimeException("Metoda MFA nu este activată!");
        }

        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        // Verificăm codul
        if (provider.verifyCode(user, code)) {
            // Aici în viitor am returna un token JWT. Acum dăm doar un mesaj de succes.
            return "LOGIN_SUCCESS_MFA";
        } else {
            throw new RuntimeException("Cod invalid!");
        }
    }
}
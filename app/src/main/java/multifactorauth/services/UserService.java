package multifactorauth.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import multifactorauth.domain.User;
import multifactorauth.domain.UserMfaMethod;
import multifactorauth.dto.LoginResponse;
import multifactorauth.dto.MfaChallengeResponse;
import multifactorauth.repo.UserMfaMethodRepository;
import multifactorauth.repo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMfaMethodRepository mfaMethodRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Strategy Pattern: Spring ne va injecta automat TOATE clasele 
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

    // --- ÎNREGISTRAREA ---
    public User registerUser(String username, String email, String rawPassword) {
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

        System.out.println("User înregistrat cu succes: " + username);
        return savedUser;
    }

    // --- LOGIN PASUL 1 (Verificarea Parolei și a Metodelor MFA) ---
    public LoginResponse loginStep1(String email, String rawPassword) {
        
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

        // 4. Returnăm un răspuns diferit în funcție de dacă are sau nu metode MFA activate
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

    // --- Obține detaliile utilizatorului curent ---
    public User getUserDetails(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));
    }

    // --- Actualizează profilul ---
    public void updateUser(String email, String currentPassword, String newUsername, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        // 1. EXTRA SECURITATE: Verificăm dacă parola curentă introdusă este corectă!
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Eroare: Parola curentă introdusă este incorectă!");
        }

        // 2. Verificăm dacă a introdus un username nou
        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(user.getUsername())) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                throw new RuntimeException("Eroare: Acest username este deja folosit!");
            }
            user.setUsername(newUsername);
        }

        // 3. Verificăm dacă a introdus o parolă nouă
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user); // Salvăm modificările
    }

    // --- CONFIGURARE MFA (Trimiterea/Generarea codului inițial) ---
    public MfaChallengeResponse setupMfaMethod(String email, String providerName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        // Verificăm dacă metoda MFA există deja pentru el în DB. Dacă nu, o creăm!
        Optional<UserMfaMethod> existingMethod = mfaMethodRepository.findByUserAndProviderName(user, providerName);
        if (existingMethod.isEmpty()) {
            UserMfaMethod newMethod = new UserMfaMethod(user, providerName, null);
            newMethod.setEnabled(false); // Încă nu e validată!
            mfaMethodRepository.save(newMethod);
        }

        // Căutăm providerul (ex: EmailMfaProvider sau TotpMfaProvider)
        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        // Nu mai este nevoie de "if". Lăsăm provider-ul ales să își facă treaba lui specifică
        // și să returneze DTO-ul direct către Controller/React!
        return provider.generateChallenge(user);
    }

    // --- CONFIRMARE MFA (Validarea codului și activarea metodei) ---
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

    // --- Trimite codul atunci când userul încearcă să se logheze ---
    public void sendLoginChallenge(String email, String providerName) {
        // Dacă metoda este TOTP, ne oprim aici! 
        // Telefonul generează codul offline, deci serverul nu trebuie să trimită/genereze nimic la logare.
        if (providerName.equals("TOTP")) {
            return; 
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nu a fost găsit!"));

        MfaProvider provider = mfaProviders.stream()
                .filter(p -> p.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Providerul " + providerName + " nu există!"));

        // Apelăm noua metodă. În cazul EMAIL, va genera un cod nou și va trimite mail-ul.
        // Ignorăm return-ul (DTO-ul) pentru că la login avem nevoie doar ca acțiunea să se întâmple în background.
        provider.generateChallenge(user);
    }

    // --- LOGIN PASUL 2 (Verificarea codului propriu-zis la logare) ---
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
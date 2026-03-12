package multifactorauth.services;

import java.util.List;
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
    public User registerUser(String username, String email, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Eroare: Acest username este deja folosit!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Eroare: Acest email este deja folosit!");
        }

        // Criptăm parola înainte să o salvăm! (Magia BCrypt)
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Creăm userul folosind parola criptată, NU textul clar
        User newUser = new User(username, email, encodedPassword);

        System.out.println("User înregistrat cu succes: " + username);
        return userRepository.save(newUser);
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
    // --- 3. CONFIGURARE MFA (Userul vrea să activeze o metodă nouă din setări) ---
    public Object setupMfaMethod(Long userId, String providerName) {
        // TODO: Găsește userul
        // TODO: Găsește provider-ul corect din lista 'mfaProviders' (ex: cel care are getProviderName() == "TOTP")
        // TODO: Generează un 'secretKey' și creează un rând nou în tabela 'user_mfa_methods' (cu is_enabled = false)
        // TODO: Dacă e TOTP, generează un QR Code URL și trimite-l la Frontend ca userul să-l scaneze
        return null;
    }

    // --- 4. CONFIRMARE MFA (Userul bagă primul cod ca să valideze configurarea) ---
    public boolean confirmMfaSetup(Long userId, String providerName, String code) {
        // TODO: Caută metoda MFA în DB care are 'is_enabled = false'
        // TODO: Apelează 'verifyCode' din provider-ul corect
        // TODO: Dacă e corect codul, setează 'is_enabled = true' în DB și salvează. Metoda e acum activă!
        return false;
    }

    // --- 5. LOGIN PASUL 2 (Verificarea codului propriu-zis) ---
    public String loginStep2(Long userId, String providerName, String code) {
        // TODO: Verifică dacă metoda aleasă de user este activă (is_enabled = true)
        // TODO: Apelează 'verifyCode' din provider-ul specific
        // TODO: Dacă codul e corect, generează un token JWT (Json Web Token) pentru ca React-ul să aibă acces la platformă
        // TODO: Returnează token-ul JWT
        return null;
    }
}
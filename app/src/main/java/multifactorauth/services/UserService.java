package multifactorauth.services;

import multifactorauth.domain.User;
import multifactorauth.repo.UserMfaMethodRepository;
import multifactorauth.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMfaMethodRepository mfaMethodRepository;
    
    // Aici e magia Strategy Pattern: Spring ne va injecta automat TOATE clasele 
    // care implementează MfaProvider (și TotpMfaProvider, și EmailMfaProvider)
    private final List<MfaProvider> mfaProviders;

    public UserService(UserRepository userRepository, 
                       UserMfaMethodRepository mfaMethodRepository, 
                       List<MfaProvider> mfaProviders) {
        this.userRepository = userRepository;
        this.mfaMethodRepository = mfaMethodRepository;
        this.mfaProviders = mfaProviders;
    }

    // // --- 1. ÎNREGISTRAREA ---
    // public User registerUser(String username, String email, String rawPassword) {
    //     // TODO: Verifică dacă username-ul sau email-ul există deja în DB
    //     // TODO: Hash-uiește parola 'rawPassword' (niciodată nu o salvăm în clar!)
    //     // TODO: Salvează și returnează noul User în baza de date
    //     return null; 
    // }

    // --- 1. ÎNREGISTRAREA ---
    public User registerUser(String username, String email, String rawPassword) {
    // 1. Verificăm dacă username-ul există deja
    if (userRepository.findByUsername(username).isPresent()) {
        throw new RuntimeException("Eroare: Acest username este deja folosit!");
    }

    // 2. Verificăm dacă email-ul există deja
    if (userRepository.findByEmail(email).isPresent()) {
        throw new RuntimeException("Eroare: Acest email este deja folosit!");
    }

    // 3. Transformăm parola în Hash (Securitate maximă)
    // BCrypt generează automat un "salt" și îl include în hash
    //String encodedPassword = passwordEncoder.encode(rawPassword);

    // 4. Creăm obiectul User
    User newUser = new User(username, email, rawPassword);

    // 5. Salvare în baza de date
    User savedUser = userRepository.save(newUser);
    
    System.out.println("User înregistrat cu succes: " + username);
    return savedUser;
}

    // --- 2. LOGIN PASUL 1 (Parola) ---
    public Object loginStep1(String username, String rawPassword) {
        // TODO: Găsește userul după username
        // TODO: Verifică dacă parola introdusă se potrivește cu hash-ul din DB
        // TODO: Caută în 'mfaMethodRepository' ce metode are activate acest user
        // TODO: Returnează către Frontend un răspuns (ex: "MFA_REQUIRED" și lista ["TOTP", "EMAIL"])
        return null;
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
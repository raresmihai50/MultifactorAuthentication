package multifactorauth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import multifactorauth.domain.User;
import multifactorauth.dto.LoginRequest;
import multifactorauth.dto.LoginResponse;
import multifactorauth.dto.RegisterRequest;
import multifactorauth.dto.UpdateRequest;
import multifactorauth.services.UserService;

@RestController // Spune Spring-ului că această clasă răspunde la cereri Web (HTTP)
@RequestMapping("/api/auth") // Toate rutele de aici vor începe cu /api/auth
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Această metodă prinde cererile de tip POST la adresa /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Trimitem cele 3 câmpuri
            userService.registerUser(
                request.getUsername(),
                request.getEmail(), 
                request.getPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Cont creat cu succes!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Metoda care verifică parola cu BCrypt
            LoginResponse response = userService.loginStep1(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Parolă greșită sau email inexistent
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    } 

    // Endpoint pentru a cere trimiterea unui cod pe email
    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@RequestParam String email, @RequestParam String provider) {
        try {
            Object response = userService.setupMfaMethod(email, provider);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint pentru a verifica codul introdus
    @PostMapping("/mfa/confirm")
    public ResponseEntity<?> confirmMfa(@RequestParam String email, @RequestParam String provider, @RequestParam String code) {
        try {
            boolean isSuccess = userService.confirmMfaSetup(email, provider, code);
            if (isSuccess) {
                return ResponseEntity.ok(Map.of("message", "MFA activat cu succes!"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Cod invalid!"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint pentru a trimite email-ul la logare
    @PostMapping("/login/challenge")
    public ResponseEntity<?> loginChallenge(@RequestParam String email, @RequestParam String provider) {
        try {
            userService.sendLoginChallenge(email, provider);
            return ResponseEntity.ok(Map.of("message", "Cod trimis cu succes!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint pentru a verifica codul și a finaliza logarea
    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@RequestParam String email, @RequestParam String provider, @RequestParam String code) {
        try {
            String result = userService.loginStep2(email, provider, code);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Returnează detaliile userului curent ca să le afișăm în Dashboard
    @GetMapping("/me")
    public ResponseEntity<?> getUserDetails(@RequestParam String email) {
        try {
            User user = userService.getUserDetails(email);
            return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateRequest request) {
        try {
            // Trimitem și parola curentă extrasă din Request
            userService.updateUser(
                request.getEmail(), 
                request.getCurrentPassword(), 
                request.getNewUsername(), 
                request.getNewPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Profil actualizat cu succes!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
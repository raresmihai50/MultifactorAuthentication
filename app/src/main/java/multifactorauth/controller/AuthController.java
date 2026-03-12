package multifactorauth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import multifactorauth.dto.RegisterRequest;
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
            // Pasăm datele primite din React către Service-ul nostru
            userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getSelectedMfas()
            );
            
            // Dacă merge bine, trimitem înapoi un JSON de succes
            return ResponseEntity.ok(Map.of("message", "Cont creat cu succes!"));
            
        } catch (RuntimeException e) {
            // Dacă dă eroare (ex: email deja folosit), trimitem eroarea la React
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
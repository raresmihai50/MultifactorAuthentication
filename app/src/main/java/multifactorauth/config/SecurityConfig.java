package multifactorauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 1. Definim "Motorul" de criptare (BCrypt)
    // Acesta va fi injectat automat de Spring în UserService-ul nostru
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Oprim securitatea automată a Spring-ului temporar
    // (Altfel, ne-ar cere o parolă generată la orice cerere, până nu implementăm noi ecranele de login)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Dezactivăm CSRF ca să putem testa mai târziu din Postman/Frontend
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Lăsăm "ușile deschise" momentan
        
        return http.build();
    }
}
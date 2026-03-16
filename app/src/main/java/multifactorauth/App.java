package multifactorauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        // Aici pornim efectiv serverul Spring Boot
        SpringApplication.run(App.class, args);
        System.out.println("Serverul a pornit cu succes! Baza de date este sincronizata.");
    }
}
// package multifactorauth;

// import multifactorauth.services.UserService;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;

// @SpringBootApplication
// public class App {

//     public static void main(String[] args) {
//         SpringApplication.run(App.class, args);
//     }

//     // Această metodă va rula AUTOMAT imediat ce serverul a pornit
//     @Bean
//     public CommandLineRunner testFlow(UserService userService) {
//         return args -> {
//             System.out.println("--- Testare înregistrare utilizator ---");
            
//             try {
//                 // Încercăm să creăm un user de test
//                 userService.registerUser("rares_test", "rares@test.com", "parola_mea_secreta");
//                 System.out.println("Utilizatorul a fost creat cu succes!");
//             } catch (Exception e) {
//                 System.out.println("Eroare la test: " + e.getMessage());
//             }

//             System.out.println("Verifică acum tabelul 'users' în baza de date!");
//         };
//     }
// }

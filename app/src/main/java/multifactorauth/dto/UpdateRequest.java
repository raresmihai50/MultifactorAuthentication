package multifactorauth.dto;

public class UpdateRequest {
    private String email; // Folosim email-ul ca să știm pe cine modificăm
    private String newUsername;
    private String newPassword;

    // Getters și Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
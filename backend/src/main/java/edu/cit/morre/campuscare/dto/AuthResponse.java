package edu.cit.morre.campuscare.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    // Default constructor
    public AuthResponse() {}

    // Constructor with just token (for backward compatibility)
    public AuthResponse(String token) {
        this.token = token;
    }

    // Full constructor
    public AuthResponse(String token, String email, String firstName, String lastName, String role) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
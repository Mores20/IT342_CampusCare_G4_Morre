package edu.cit.morre.campuscare.dto;

public class ProfileResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String role;

    public ProfileResponse(String firstName, String lastName, String email, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
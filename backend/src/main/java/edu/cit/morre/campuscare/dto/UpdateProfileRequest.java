package edu.cit.morre.campuscare.dto;

import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
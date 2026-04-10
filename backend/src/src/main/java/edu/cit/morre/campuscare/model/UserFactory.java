package edu.cit.morre.campuscare.model;

import org.springframework.security.crypto.password.PasswordEncoder;

public class UserFactory {

    public static User createLocalUser(String name, String email, String password, PasswordEncoder encoder) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        return user;
    }

    public static User createOAuthUser(String name, String email, PasswordEncoder encoder) {
        User user = new User();
        user.setName(name != null ? name : "OAuth User");
        user.setEmail(email);
        user.setPassword(encoder.encode("oauth2-user"));
        return user;
    }
}
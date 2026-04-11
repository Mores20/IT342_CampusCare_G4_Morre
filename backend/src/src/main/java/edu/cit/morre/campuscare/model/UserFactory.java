package edu.cit.morre.campuscare.model;

import org.springframework.security.crypto.password.PasswordEncoder;

public class UserFactory {

    public static User createLocalUser(String firstName, String lastName, String email,
                                       String password, PasswordEncoder encoder) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setProvider("local");
        return user;
    }

    public static User createOAuthUser(String name, String email, PasswordEncoder encoder) {
        String[] parts = name != null ? name.split(" ", 2) : new String[]{"OAuth", "User"};
        User user = new User();
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");
        user.setEmail(email);
        user.setPassword(encoder.encode("oauth2-user"));
        user.setProvider("google");
        return user;
    }
}
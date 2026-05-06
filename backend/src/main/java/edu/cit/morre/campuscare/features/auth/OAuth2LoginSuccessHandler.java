package edu.cit.morre.campuscare.features.auth;

import edu.cit.morre.campuscare.features.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component  // Make sure this annotation is present
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final String frontendUrl = "http://localhost:3000";

    public OAuth2LoginSuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof OAuth2User oAuth2User)) {
            response.sendRedirect(frontendUrl + "/auth/callback?error=oauth2_principal_invalid");
            return;
        }

        try {
            AuthResponse authResponse = authService.authenticateWithGoogleOAuth2User(oAuth2User);

            String encodedToken = URLEncoder.encode(
                    authResponse.getToken(),
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(frontendUrl + "/auth/callback?token=" + encodedToken);

        } catch (IllegalArgumentException ex) {
            String encodedMessage = URLEncoder.encode(
                    ex.getMessage(),
                    StandardCharsets.UTF_8
            );
            response.sendRedirect(frontendUrl + "/auth/callback?error=" + encodedMessage);
        }
    }
}
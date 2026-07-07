package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.entities.User;
import org.example.backend.repos.UserRepository;
import org.example.backend.services.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof OAuth2User oAuth2User)) {
            throw new IllegalStateException("Expected OAuth2User principal");
        }

        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("OAuth2 user not found after login: " + email));

        String sessionToken = userService.issueSessionToken(user);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", sessionToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}

package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.example.backend.entities.User;
import org.example.backend.repos.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String EMAIL_ATTRIBUTE = "email";

    private final UserRepository userRepository;
    private final RestClient githubRestClient;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest request) {
        OAuth2User oAuth2User = fetchOAuth2User(request);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String email = (String) attributes.get(EMAIL_ATTRIBUTE);
        if (email == null) {
            email = fetchPrimaryVerifiedEmail(request.getAccessToken().getTokenValue());
        }
        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "GitHub account has no accessible verified email address");
        }

        attributes.put(EMAIL_ATTRIBUTE, email);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Account already exists with another authentication method
            if (user.getPassword() != null || !"github".equalsIgnoreCase(user.getProvider())) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("account_conflict"),
                        "An account with this email already exists using a different sign-in method.");
            }
        } else {
            createGithubUser(email, attributes);
        }

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "id"
        );
    }

    OAuth2User fetchOAuth2User(OAuth2UserRequest request) {
        return delegate.loadUser(request);
    }

    private void createGithubUser(String email, Map<String, Object> attributes) {
        String login = String.valueOf(attributes.get("login"));
        String name = attributes.get("name") != null ? String.valueOf(attributes.get("name")) : login;

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(uniqueUsername(login, attributes.get("id")));
        user.setProvider("github");
        userRepository.save(user);
    }

    private String uniqueUsername(String login, Object githubId) {
        if (!userRepository.existsByUsername(login)) {
            return login;
        }
        return login + "-" + githubId;
    }

    private String fetchPrimaryVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails = githubRestClient.get()
                .uri("/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}); // use generic type ParameterizedTypeReference

        if (emails == null) {
            return null;
        }

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                .map(e -> (String) e.get(EMAIL_ATTRIBUTE))
                .findFirst()
                .orElse(null);
    }
}

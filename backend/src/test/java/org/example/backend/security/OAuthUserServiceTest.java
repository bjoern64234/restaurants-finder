package org.example.backend.security;

import org.example.backend.entities.User;
import org.example.backend.repos.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestClient githubRestClient;

    private OAuth2UserRequest buildUserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "test-access-token", Instant.now(), Instant.now().plusSeconds(3600));

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    private OAuth2User stubbedGithubUser(Map<String, Object> attributes) {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
    }

    private OAuthUserService serviceReturning(OAuth2User stubbedUser) {
        return new OAuthUserService(userRepository, githubRestClient) {
            @Override
            OAuth2User fetchOAuth2User(OAuth2UserRequest request) {
                return stubbedUser;
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockGithubEmailsResponse(List<Map<String, Object>> emails) {
        RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec headersSpec = mock(RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(githubRestClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/user/emails")).thenReturn(headersSpec);
        when(headersSpec.header(eq("Authorization"), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(emails);
    }

    @Test
    void loadUser_returnsExistingUser_withoutCreatingNewOne_whenEmailAttributePresent() {
        Map<String, Object> attributes = Map.of("id", "12345", "email", "jane@example.com", "login", "janedoe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(new User()));

        OAuth2User result = service.loadUser(buildUserRequest());

        assert result != null;

        String email = result.getAttribute("email");

        assertThat(email).isNotNull();
        assertThat(email).isEqualTo("jane@example.com");
    }

    @Test
    void loadUser_createsNewGithubUser_whenEmailPresent_andUserDoesNotExist() {
        Map<String, Object> attributes = Map.of(
                "id", "12345", "email", "jane@example.com", "login", "janedoe", "name", "Jane Doe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);

        service.loadUser(buildUserRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Jane Doe");
        assertThat(savedUser.getEmail()).isEqualTo("jane@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("janedoe");
        assertThat(savedUser.getProvider()).isEqualTo("github");
    }

    @Test
    void loadUser_fallsBackToLogin_forName_whenNameAttributeMissing() {
        Map<String, Object> attributes = Map.of("id", "12345", "email", "jane@example.com", "login", "janedoe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);

        service.loadUser(buildUserRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("janedoe");
    }

    @Test
    void loadUser_appendsGithubId_toUsername_whenUsernameAlreadyTaken() {
        Map<String, Object> attributes = Map.of("id", "12345", "email", "jane@example.com", "login", "janedoe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        service.loadUser(buildUserRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("janedoe-12345");
    }

    @Test
    void loadUser_fetchesEmailFromGithubApi_whenEmailAttributeMissing() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        attributes.put("login", "janedoe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        mockGithubEmailsResponse(List.of(
                Map.of("email", "unverified@example.com", "primary", false, "verified", true),
                Map.of("email", "verified@example.com", "primary", true, "verified", true)
        ));
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);

        OAuth2User result = service.loadUser(buildUserRequest());

        assert result != null;
        assertThat(result.<String>getAttribute("email")).isEqualTo("verified@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("verified@example.com");
    }

    @Test
    void loadUser_throwsOAuth2AuthenticationException_whenNoVerifiedEmailAvailable() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "12345");
        attributes.put("login", "janedoe");
        OAuthUserService service = serviceReturning(stubbedGithubUser(attributes));

        mockGithubEmailsResponse(List.of(
                Map.of("email", "unverified@example.com", "primary", true, "verified", false)
        ));

        assertThatThrownBy(() -> service.loadUser(buildUserRequest()))
                .isInstanceOf(OAuth2AuthenticationException.class);

        verifyNoInteractions(userRepository);
    }
}

package org.example.backend.configurations;

import lombok.RequiredArgsConstructor;
import org.example.backend.security.OAuth2AuthenticationSuccessHandler;
import org.example.backend.security.OAuthUserService;
import org.example.backend.security.SessionTokenAuthenticationFilter;
import org.example.backend.services.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthUserService oAuthUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final SessionService sessionService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // Safe to disable CSRF because:
                // - REST API is stateless
                // - Authentication uses bearer/session tokens in headers, not cookies
                // - Browsers do not automatically attach Authorization headers
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/session", "/api/logout", "/api/restaurants").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/restaurants").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new SessionTokenAuthenticationFilter(sessionService), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuthUserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((_, response, exception) ->
                                response.sendRedirect(UriComponentsBuilder.fromUriString(frontendUrl)
                                        .queryParam("authError", exception.getMessage())
                                        .encode()
                                        .build()
                                        .toUriString()))
                );

        return http.build();
    }
}

package com.claims.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errors) throws Exception {
        // Sends security errors to ApiExceptionHandler, so every error has the same JSON format.
        AuthenticationEntryPoint unauthorized = (request, response, e) ->
                errors.resolveException(request, response, null, e);
        return http
                // No cookies or sessions are used, so CSRF does not apply.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((request, response, denied) ->
                                errors.resolveException(request, response, null, denied)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/claims", "/api/claims/*/info-response")
                        .hasRole("CLAIMANT")
                        .requestMatchers(HttpMethod.GET, "/api/me", "/api/claims", "/api/claims/*").authenticated()
                        .requestMatchers("/api/**").hasRole("OFFICER")
                        .anyRequest().denyAll())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // shortcut: fixed demo users for local use, replace with OAuth 2 / OIDC before any real deployment
    @Bean
    UserDetailsService users(PasswordEncoder encoder, @Value("${app.demo-password}") String password) {
        String hash = encoder.encode(password);
        return new InMemoryUserDetailsManager(
                user("alice", hash, "CLAIMANT"), user("bob", hash, "CLAIMANT"),
                user("olivia", hash, "OFFICER"), user("omar", hash, "OFFICER"));
    }

    private static UserDetails user(String name, String hash, String role) {
        return User.withUsername(name).password(hash).roles(role).build();
    }
}

package com.example.pricetag.config;

import com.example.pricetag.enums.AppUserRole;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration.
 * <p>
 * How to read this file, top to bottom:
 * 1. Three URL lists below decide WHO can call WHICH endpoints.
 * 2. securityFilterChain() wires those lists into Spring Security and
 * tells it how to check passwords (authenticationProvider) and how
 * to check JWTs (jwtFilter).
 * 3. The rest are small supporting beans (password hashing, CORS).
 * <p>
 * IMPORTANT PATTERN NOTE:
 * These arrays use Ant-style path patterns, NOT Spring MVC's {name} URI
 * template syntax. That means:
 * - "*"  matches exactly one path segment (e.g. "/product/*" matches
 * "/product/5" but not "/product/5/edit")
 * - "**" matches any number of segments
 * - "{categoryId}" is NOT special here — Spring Security would treat it
 * as a literal string and it would never match a real numeric id.
 * Always use "*" or "**" in these three arrays, never "{...}".
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Anyone can call these without logging in.
    private static final String[] PUBLIC_ENDPOINTS = {"/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/verify-otp", "/api/v1/auth/forgot-password", "/api/v1/auth/verify-forgot-password-otp", "/api/v1/auth/welcome", "/", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/api/v1/payments/stripe/webhook"};

    // Only users with ROLE_ADMIN can call these.
    private static final String[] ADMIN_ENDPOINTS = {"/api/v1/category/add", "/api/v1/subcategory/*/add", "/api/v1/subcategory/edit", "/api/v1/product/category/*/subcategory/*/add", // was "{categoryId}" - fixed, see note above
                                                     "/api/v1/image/upload", "/api/v1/variant/edit/*", "/api/v1/variant/product/*"};

    // Any logged-in user (USER or ADMIN) can call these.
    private static final String[] USER_ENDPOINTS = {"/user/**", "/api/v1/category/*", "/api/v1/subcategory/*", "/api/v1/product/*", "/api/v1/variant/", "/api/v1/variant/*", "/api/v1/orders/*"};

    // Turns AppUserRole.ROLE_ADMIN into "ADMIN", because Spring's hasRole()
    // adds the "ROLE_" prefix back on automatically - passing "ROLE_ADMIN"
    // into hasRole() would incorrectly look for "ROLE_ROLE_ADMIN".
    private String extractRole(AppUserRole role) {
        return role
                .name()
                .substring(role
                                   .name()
                                   .indexOf("_") + 1);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   PasswordEncoder passwordEncoder,
                                                   UserDetailsService userDetailsService,
                                                   JwtFilter jwtFilter) throws Exception {
        ColorLogger.logInfo("Configuring security filter chain");
        return httpSecurity
                // CSRF protection is for browser cookie/session-based apps.
                // Safe to disable here because we're stateless + JWT-based,
                // not cookie-based - there's no session for an attacker to ride on.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS)
                        .hasRole(extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(USER_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        // Anything not listed above still requires SOME logged-in
                        // user. This is your safety net - new endpoints are locked
                        // down by default until you explicitly open them up above.
                        .anyRequest()
                        .authenticated())
                // No HTTP sessions - every request must prove itself with a JWT.
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Used for the email+password login step (see AuthService.login).
                .authenticationProvider(authenticationProvider(passwordEncoder, userDetailsService))
                // Runs before Spring's default login filter, reads the JWT on
                // every request and, if valid, logs the user in for that request.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 - each increment roughly doubles hashing
        // time, which is the point (slows down brute-force attempts).
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        // This is the object AuthService calls to actually verify email+password.
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder,
                                                         UserDetailsService userDetailsService) {
        // DaoAuthenticationProvider = "look the user up via UserDetailsService,
        // then compare the submitted password against the stored hash using
        // the given PasswordEncoder."
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(
                userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Only these origins may call the API from a browser.
        // Update this list as your frontend domains change.
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:3000", "http://localhost:8080",
                              "https://yourdomain.com"));

        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
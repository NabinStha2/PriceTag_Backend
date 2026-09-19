package com.example.pricetag.config;

import com.example.pricetag.enums.AppUserRole;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * Endpoint access rules, grouped by HTTP method and required role.
 * Keep these paths aligned with controller mappings. A single '*' matches
 * one path segment; reads and writes to the same path have separate rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_GET_ENDPOINTS = {"/", "/api/v1/auth/welcome", "/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"};

    private static final String[] PUBLIC_POST_ENDPOINTS = {"/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/verify-otp", "/api/v1/auth/forgot-password", "/api/v1/auth/refreshToken", "/api/v1/payments/stripe/webhook"};

    // Catalog changes are admin-only; catalog reads below still require login.
    private static final String[] ADMIN_POST_ENDPOINTS = {"/api/v1/category/add", "/api/v1/subcategory/*/add", "/api/v1/product/category/*/subcategory/*/add", "/api/v1/variant/product/*"};

    private static final String[] ADMIN_PATCH_ENDPOINTS = {"/api/v1/category/update/*", "/api/v1/subcategory/edit", "/api/v1/product/*", "/api/v1/variant/edit/*"};

    private static final String[] ADMIN_DELETE_ENDPOINTS = {"/api/v1/category/*", "/api/v1/subcategory/*", "/api/v1/product/*", "/api/v1/variant/"};

    private static final String[] USER_GET_ENDPOINTS = {"/api/v1/user/", "/api/v1/category/", "/api/v1/category/*", "/api/v1/subcategory", "/api/v1/subcategory/*", "/api/v1/product", "/api/v1/product/*", "/api/v1/product/subcategory/*", "/api/v1/product/subcategory/*/search", "/api/v1/variant/", "/api/v1/variant/*", "/api/v1/variant/product/*", "/api/v1/cart", "/api/v1/address", "/api/v1/orders", "/api/v1/orders/*", "/api/v1/rating-review/*", "/api/v1/rating-review/product/*", "/api/v1/rating-review/user/product/*"};

    private static final String[] USER_POST_ENDPOINTS = {"/api/v1/cart/items", "/api/v1/address", "/api/v1/orders", "/api/v1/orders/*/cancel", "/api/v1/orders/*/payment-intent", "/api/v1/rating-review/add/product/*"};

    private static final String[] USER_PATCH_ENDPOINTS = {"/api/v1/user/edit", "/api/v1/cart/item/*", "/api/v1/address/*", "/api/v1/address/*/default", "/api/v1/rating-review/update/*"};

    private static final String[] USER_DELETE_ENDPOINTS = {"/api/v1/cart", "/api/v1/cart/item/*", "/api/v1/address/*", "/api/v1/rating-review/*"};

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
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(HttpMethod.HEAD, PUBLIC_GET_ENDPOINTS)
                        .permitAll()
                        // Refresh tokens and Stripe signatures are validated by their services.
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH,
                                         "/api/v1/auth/verify-forgot-password-otp")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, ADMIN_POST_ENDPOINTS)
                        .hasRole(extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.PATCH, ADMIN_PATCH_ENDPOINTS)
                        .hasRole(extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.DELETE, ADMIN_DELETE_ENDPOINTS)
                        .hasRole(extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.GET, USER_GET_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.HEAD, USER_GET_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.POST, USER_POST_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.PATCH, USER_PATCH_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        .requestMatchers(HttpMethod.DELETE, USER_DELETE_ENDPOINTS)
                        .hasAnyRole(extractRole(AppUserRole.ROLE_USER),
                                    extractRole(AppUserRole.ROLE_ADMIN))
                        // New endpoints require an explicit access rule before use.
                        .anyRequest()
                        .denyAll())
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

package com.example.pricetag.config;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.auth.service.JwtService;
import com.example.pricetag.utils.ColorLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request (before Spring's normal login filter - see
 * SecurityConfig.addFilterBefore). Its job:
 * 1. Look for "Authorization: Bearer <token>".
 * 2. If present and valid, tell Spring Security "this request is from
 * this user" for the rest of the request.
 * 3. If missing, do nothing and let the request continue - later,
 * SecurityConfig's authorizeHttpRequests rules will reject it if the
 * endpoint required a logged-in user.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userName = null;

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            token = authHeader.substring(7);
            try {
                userName = jwtService.extractUserName(token);
            } catch (ExpiredJwtException e) {
                writeUnauthorized(response, "Token expired");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                // Covers malformed tokens, bad signatures, tampered tokens,
                // and empty/garbage strings - anything that isn't a valid
                // JWT signed with our secret. Without this catch, a request
                // with a broken token would crash with a 500 instead of a
                // clean 401.
                ColorLogger.logError("Invalid JWT: " + e.getMessage());
                writeUnauthorized(response, "Invalid token");
                return;
            }
        }

        if (userName != null && SecurityContextHolder
                                        .getContext()
                                        .getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        CommonResponseDto<String> responseDto = CommonResponseDto
                .<String>builder()
                .success(false)
                .message(message)
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
    }
}
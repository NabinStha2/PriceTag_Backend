package com.example.pricetag.config;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.services.JwtService;
import com.example.pricetag.utils.ColorLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.util.logging.Logger;

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
        ColorLogger.logInfo("authHeader :: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            token = authHeader.substring(7);
            try {
                userName = jwtService.extractUserName(token);
            } catch (ExpiredJwtException e) {
                ColorLogger.logError(e.toString());
                response.setContentType("application/json");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                CommonResponseDto<String> responseDto = CommonResponseDto
                        .<String>builder()
                        .success(false)
                        .message("Token expired")
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .data(null)
                        .build();
//                ObjectMapper mapper = new ObjectMapper();
//                response.getWriter().write(mapper.writeValueAsString(responseDto));
                new ObjectMapper().writeValue(response.getOutputStream(), responseDto);
                return;
            }
        }
        if (userName != null && SecurityContextHolder
                                        .getContext()
                                        .getAuthentication() == null) {
            ColorLogger.logInfo("I am inside JwtFilter :: " + userName);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            if (jwtService.validateToken(token, userDetails)) {
                Logger
                        .getLogger(getClass().getName())
                        .info("I am inside JwtFilter validateToken :: " +
                              userDetails.getUsername());
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

}
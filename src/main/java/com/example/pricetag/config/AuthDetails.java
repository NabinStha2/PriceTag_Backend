package com.example.pricetag.config;

import com.example.pricetag.features.auth.entity.Role;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.utils.ColorLogger;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Adapts our own User entity to the interface Spring Security expects
 * (UserDetails). This is the "principal" object that ends up in
 * SecurityContextHolder once a request is authenticated - see
 * AuthService.getUser() / getCurrentUserId() for how it's read back out.
 */
public class AuthDetails implements UserDetails {
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isActive;
    private final String userName;
    private final String password;
    @Getter
    private final Long id;

    public AuthDetails(User user) {
        this.userName = user.getEmail();
        this.password = user.getPassword();
        this.id = user.getId();
        this.isActive = user.getIsActive();

        // Role.getName() is expected to already be in "ROLE_XXX" form
        // (e.g. "ROLE_ADMIN") to match what SecurityConfig's hasRole(...)
        // checks look for.
        this.authorities = user
                .getRoles()
                .stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        ColorLogger.logInfo("AuthDetails :: User :: " + userName + " Roles :: " + authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Deactivated users (isActive = false) get rejected right at
        // authentication time - they never even get a token.
        return isActive;
    }
}
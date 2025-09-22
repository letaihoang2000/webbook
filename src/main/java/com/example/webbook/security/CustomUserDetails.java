package com.example.webbook.security;

import com.example.webbook.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return user's role as authority with "ROLE_" prefix (Spring Security convention)
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Using email as username
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
        return true;
    }

    // Get the actual User object
    public User getUser() {
        return user;
    }

    // Helper method to get the role name
    public String getRoleName() {
        return user.getRole().getRoleName();
    }

    // Helper method to get full name
    public String getFullName() {
        return user.getFirst_name() + " " + user.getLast_name();
    }
}

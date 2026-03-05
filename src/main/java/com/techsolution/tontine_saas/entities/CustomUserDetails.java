package com.techsolution.tontine_saas.entities;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final Long userId;

    public CustomUserDetails(Long userId, String username, @Nullable String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    // ✅ AJOUT UTILE
    public boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}

package com.techsolution.tontine_saas.entities;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.lang.Nullable;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long userId;
    private final Long associationId;

    public CustomUserDetails(
            Long userId,
            String email,
            @Nullable String password,
            boolean active,
            Long associationId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        // Spring Security User ne supporte pas un mot de passe null si on utilise certaines
        // stratégies d'auth. On passe une chaîne vide ou "N/A" si null.
        super(
                email,
                password != null ? password : "N/A",
                active,
true,
true,
true,
                authorities
        );
        this.userId = userId;
        this.associationId = associationId;
    }

    /**
     * Helper pour le mapping depuis l'entité User.
     */
    public String getEmail() {
        return super.getUsername();
    }

    /**
     * Vérifie si l'utilisateur possède une autorité spécifique.
     */
    public boolean hasRole(String role) {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }
}
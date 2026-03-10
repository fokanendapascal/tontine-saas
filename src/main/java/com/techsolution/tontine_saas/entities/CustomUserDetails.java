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
            Long associationId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        // Spring Security User ne supporte pas un mot de passe null si on utilise certaines
        // stratégies d'auth. On passe une chaîne vide ou "N/A" si null.
        super(email, password != null ? password : "N/A", authorities);
        this.userId = userId;
        this.associationId = associationId;
    }

    // Spring Security utilise getUsername() pour l'email dans votre cas
    public String getEmail() {
        return super.getUsername();
    }

    /**
     * Vérifie si l'utilisateur possède un rôle spécifique.
     * @param authority Le rôle avec le préfixe (ex: 'ROLE_ADMIN')
     */
    public boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * Vérifie si l'utilisateur est un administrateur.
     */
    public boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }
}
package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.entities.CustomUserDetails;
import com.techsolution.tontine_saas.exceptions.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    /**
     * Récupère l'ID de l'association de l'utilisateur connecté.
     * Si l'utilisateur est SUPER_ADMIN, il pourrait ne pas être rattaché
     * à une association classique.
     */
    public static Long getCurrentAssociationId() {
        return getCustomUserDetails()
                .map(CustomUserDetails::getAssociationId)
                .orElseThrow(() -> new BaseException("association.access.denied", "ASSOCIATION_ID_NOT_FOUND", HttpStatus.FORBIDDEN));
    }

    /**
     * Récupère l'ID de l'utilisateur connecté.
     */
    public static Long getCurrentUserId() {
        return getCustomUserDetails()
                .map(CustomUserDetails::getUserId)
                .orElseThrow(() -> new BaseException("unauthorized", "USER_NOT_FOUND_IN_SESSION", HttpStatus.UNAUTHORIZED));
    }

    /**
     * Vérifie si l'utilisateur actuel possède un rôle spécifique.
     */
    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    /**
     * Méthode privée pour extraire le Principal de manière sécurisée.
     */
    private static Optional<CustomUserDetails> getCustomUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

}

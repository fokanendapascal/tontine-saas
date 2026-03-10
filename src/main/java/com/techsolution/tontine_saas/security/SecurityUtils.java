package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.entities.CustomUserDetails;
import com.techsolution.tontine_saas.exceptions.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Récupère l'ID de l'association de l'utilisateur connecté.
     * Très utile pour filtrer les requêtes SQL par association.
     */
    public static Long getCurrentAssociationId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getAssociationId();
        }
        throw new BaseException("unauthorized", "SESSION_EXPIRED", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Récupère l'ID de l'utilisateur connecté.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getUserId();
        }
        return null;
    }
}

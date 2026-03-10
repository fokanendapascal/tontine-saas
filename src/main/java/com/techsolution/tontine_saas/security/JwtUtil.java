package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.access-exp-ms:900000}")   // 15 min
    private long expirationMs;
    @Value("${security.jwt.refresh-exp-ms:1209600000}") // 14 jours
    private long refreshExpMs;

    // Utilisation de SecretKey pour la compatibilité avec les nouvelles versions
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, List<String> roles, Long associationId) {
        return Jwts.builder()
                .subject(email) // Définit le "sub"
                .claim("roles", roles)
                .claim("associationId", associationId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey()) // Signature automatique
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("typ", "refresh")
                .claim("associationId", user.getAssociation().getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpMs)))
                .signWith(getSigningKey())
                .compact();
    }

    // 🔥 Nouveau : Extraction de l'ID de l'association
    public Long extractAssociationId(String token) {
        Integer id = extractClaim(token, claims -> claims.get("associationId", Integer.class));
        return id != null ? id.longValue() : null;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Méthode générique pour extraire un claim spécifique
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction des claims : {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // 1. Vérification de la signature et de l'expiration
            final String username = extractEmail(token);

            // 2. Vérification de l'identité : l'email du token doit correspondre à l'user chargé
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("JWT non valide : {}", e.getMessage());
            return false;
        }
    }

    // Méthode utilitaire interne
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT non valide ou expiré : {}", e.getMessage());
            return false;
        }
    }
}

package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-exp-ms:900000}") // 15 minutes
    private long accessExpirationMs;

    @Value("${security.jwt.refresh-exp-ms:1209600000}") // 14 jours
    private long refreshExpirationMs;

    /*
     * ==============================
     * SIGNING KEY
     * ==============================
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    // Utilisation de SecretKey pour la compatibilité avec les nouvelles versions
    //private SecretKey getSigningKey() {
    //    return Keys.hmacShaKeyFor(secret.getBytes());
    //}

    /*
     * ==============================
     * TOKEN GENERATION
     * ==============================
     */

    public String generateToken(String email, List<String> roles, Long associationId) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .claim("associationId", associationId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("typ", "refresh")
                .claim("associationId", user.getAssociation().getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /*
     * ==============================
     * CLAIM EXTRACTION
     * ==============================
     */

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {

        try {
            Claims claims = extractAllClaims(token);
            return resolver.apply(claims);

        } catch (ExpiredJwtException ex) {

            log.warn("JWT expiré : {}", ex.getMessage());
            return null;

        } catch (JwtException ex) {

            log.warn("JWT invalide : {}", ex.getMessage());
            return null;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractAssociationId(String token) {

        Integer id = extractClaim(token,
                claims -> claims.get("associationId", Integer.class));

        return id != null ? id.longValue() : null;
    }

    /*
     * ==============================
     * TOKEN VALIDATION
     * ==============================
     */

    public boolean isTokenValid(String token, UserDetails userDetails) {

        try {

            final String email = extractEmail(token);

            return email != null
                    && email.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (Exception ex) {

            log.warn("JWT non valide : {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (ExpiredJwtException ex) {

            log.warn("JWT expiré : {}", ex.getMessage());
            return false;

        } catch (JwtException ex) {

            log.warn("JWT invalide : {}", ex.getMessage());
            return false;
        }
    }

    /*
     * ==============================
     * EXPIRATION
     * ==============================
     */

    private boolean isTokenExpired(String token) {

        Date expiration = extractClaim(token, Claims::getExpiration);

        return expiration == null || expiration.before(new Date());
    }
}

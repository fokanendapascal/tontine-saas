package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.services.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {

            // Extraction des claims une seule fois
            Claims claims = jwtUtil.extractAllClaims(jwt);
            String userEmail = claims.getSubject();
            Date expiration = claims.getExpiration();
            Integer assocId = claims.get("associationId", Integer.class);
            Long associationId = assocId != null ? assocId.longValue() : null;

            if (userEmail != null
                    && expiration != null
                    && expiration.after(new Date())
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Bonne pratique Spring Security
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Stockage associationId dans les détails
                    Map<String, Object> details = new HashMap<>();
                    details.put("associationId", associationId);
                    authToken.setDetails(details);

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);

                    log.debug("Authentification réussie : {} (Association ID: {})",
                            userEmail, associationId);
                }
            }

        } catch (ExpiredJwtException ex) {

            log.warn("JWT expiré : {}", ex.getMessage());

        } catch (JwtException ex) {

            log.warn("JWT invalide : {}", ex.getMessage());

        } catch (Exception ex) {

            log.error("Erreur inattendue dans JwtAuthFilter : {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

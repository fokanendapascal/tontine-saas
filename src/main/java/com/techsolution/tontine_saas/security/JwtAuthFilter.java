package com.techsolution.tontine_saas.security;

import com.techsolution.tontine_saas.services.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        System.out.println(">>> JWT FILTER CALLED");
        System.out.println("HEADER = " + request.getHeader("Authorization"));

        // 🔹 Exclure Swagger et les endpoints d'auth PUBLICS uniquement
        if (path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Exclure OPTIONS (préflight CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ---------------------------------------
        // 🔐 4. Traitement normal du JWT
        // ---------------------------------------
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            // Ajoutez ceci pour voir le coupable :
            log.info("TOKEN BRUT RECU: [{}]", token);
            try {
                email = jwtUtil.extractEmail(token);
                log.info("EMAIL EXTRAIT: {}", email);
            } catch (Exception e) {
                log.warn("Erreur extraction email depuis JWT: {}", e.getMessage());
                log.error("ERREUR D'EXTRACTION: {}", e.getMessage());
            }
        }else {
            log.debug("Pas de token Bearer dans l’en-tête pour : {}", path);
        }


        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isTokenValid(token)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("User [{}] authenticated with roles: {}", email,
                        userDetails.getAuthorities().stream()
                                .map(a -> a.getAuthority())
                                .toList());
            } else {
                log.warn("JWT invalid for email: {}", email);
            }

        } else if (email == null) {
            log.warn("No email extracted from token for request: {}", request.getServletPath());
        }

        filterChain.doFilter(request, response);
    }
}

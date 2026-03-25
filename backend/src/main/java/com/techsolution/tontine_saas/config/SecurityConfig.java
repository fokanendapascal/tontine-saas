package com.techsolution.tontine_saas.config;

import com.techsolution.tontine_saas.security.AuthEntryPointJwt;
import com.techsolution.tontine_saas.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permet d'utiliser @PreAuthorize sur vos contrôleurs
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(unauthorizedHandler)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * ===============================
                         * SWAGGER & DOCUMENTATION
                         * ===============================
                         */
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        /*
                         * ===============================
                         * AUTHENTICATION
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        /*
                         * ===============================
                         * ASSOCIATIONS
                         * ===============================
                         */
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/associations/create"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/associations/**"
                        ).hasRole("ADMIN")

                        /*
                         * ===============================
                         * USERS
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/users/**"
                        ).hasRole("ADMIN")

                        /*
                         * ===============================
                         * TONTINES
                         * ===============================
                         */
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tontines/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/tontines/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * MEMBERS TONTINES
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/members-tontines/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * CONTRIBUTIONS
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/contributions/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * SAVINGS
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/savings/**",
                                "/api/v1/savings-transactions/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * LOANS
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/loans/**",
                                "/api/v1/repayments/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * DASHBOARD
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/dashboard/**"
                        ).hasAnyRole("ADMIN", "MEMBER")

                        /*
                         * ===============================
                         * AUDIT LOGS
                         * ===============================
                         */
                        .requestMatchers(
                                "/api/v1/audit-logs/**"
                        ).hasRole("SUPER_ADMIN")

                        /*
                         * ===============================
                         * TOUT LE RESTE
                         * ===============================
                         */
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * ===============================
     * ROLE HIERARCHY
     * ===============================
     */

    @Bean
    public RoleHierarchy roleHierarchy() {

        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("SUPER_ADMIN").implies("ADMIN")
                .role("ADMIN").implies("MEMBER")
                .build();
    }

    /*
     * ===============================
     * CORS
     * ===============================
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /*
     * ===============================
     * PASSWORD ENCODER
     * ===============================
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * ===============================
     * AUTHENTICATION MANAGER
     * ===============================
     */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /*
     * ===============================
     * MESSAGES (i18n)
     * ===============================
     */

    @Bean
    public MessageSource messageSource() {

        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }
}

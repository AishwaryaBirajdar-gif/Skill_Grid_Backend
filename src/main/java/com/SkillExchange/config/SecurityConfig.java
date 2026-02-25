package com.SkillExchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // New Import
import org.springframework.web.cors.CorsConfigurationSource; // New Import
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // New Import

import com.SkillExchange.auth.JwtAuthFilter;
import com.SkillExchange.service.CustomUserDetailsService;

import java.util.Arrays; // New Import

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // NEW BEAN: Explicitly defines CORS settings for development
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // **IMPORTANT: For local development, allow the React port 5173**
        // You can change this to Arrays.asList("*") if needed, but defining
        // the specific local development port is best practice.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // Allow cookies/auth headers
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all paths
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Common for stateless REST APIs)
            .csrf(csrf -> csrf.disable())

            // 2. Explicitly apply the CorsConfigurationSource bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Define authorization rules (Using modern authorizeHttpRequests)
            .authorizeHttpRequests(auth -> auth
                // Allow public access to all /api/auth endpoints (signup, login)
                // This is the /403 fix: allowing signup/login without auth
                .requestMatchers("/api/auth", "/api/auth/**").permitAll()

                // Define authenticated endpoints
                .requestMatchers("/api/patient/**").authenticated()
                .requestMatchers("/api/doctor/**").authenticated()
                .requestMatchers("/api/order/**").authenticated()
                .requestMatchers("/api/medicine-request/**").authenticated()
                .requestMatchers("/api/medicines/**").authenticated()
                .requestMatchers("/api/appointments/**").authenticated()
                .requestMatchers("/api/prescriptions/**").authenticated()
                .requestMatchers("/api/consultation**").authenticated()

                // Role-based access (Example: specific roles for medical store)
                .requestMatchers("/api/medicalstore/**").hasAnyRole("PATIENT", "DOCTOR", "PHARMACIST")
                
                // Protect all other endpoints by requiring authentication
                .anyRequest().authenticated()
            )

            // 4. Configure session management to be stateless (for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 5. Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
package com.counterfeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig handles the security configuration for our Spring Boot application.
 * 
 * @Configuration tells Spring this class contains bean definitions (configurations).
 * @EnableWebSecurity enables Spring Security's web security support and provides the Spring Security filter chain.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @Bean tells Spring to manage this object and make it available for dependency injection.
     * PasswordEncoder is used to securely hash user passwords.
     * BCrypt is a strong, widely-used hashing algorithm.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain which controls how HTTP requests are secured.
     * Using the modern lambda DSL style for Spring Security 6.x.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery). Usually disabled when using APIs / stateless tokens.
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS (Cross-Origin Resource Sharing) to allow our frontend to communicate with this backend.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules: Currently allowing all requests without authentication (permit all).
            // This is just a starting point; in a production app, you would secure endpoints and use JWT.
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            
            // Disable the default Spring Security login page since we have our own AuthController.
            .formLogin(form -> form.disable())
            
            // Disable basic authentication (login via browser popup).
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Defines our custom CORS settings.
     * This tells the browser that it's safe for our frontend (running on a different port)
     * to make requests to this backend.
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins (e.g., http://localhost:3000, http://localhost:5173, etc.)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow all standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers in the requests
        configuration.setAllowedHeaders(List.of("*"));
        
        // Disable credentials (cookies, HTTP authentication) for simpler cross-origin requests for now
        configuration.setAllowCredentials(false);

        // Apply this configuration to all paths (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

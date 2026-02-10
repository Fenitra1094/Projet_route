package com.example.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // API endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                // Swagger UI and API docs
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/docs").permitAll()
                .requestMatchers("/api-docs").permitAll()
                // Static resources
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Root and error pages
                .requestMatchers("/").permitAll()
                .requestMatchers("/error").permitAll()
                // Require authentication for everything else
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

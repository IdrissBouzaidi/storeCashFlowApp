package com.idApps.userApi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable() // 👈 Désactive le CSRF
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/login/**", "api/v1/admin/**", "/swagger-ui/**", "api-docs/**").permitAll()
                    .requestMatchers("/api/**").authenticated()            // routes sécurisées
                    .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}
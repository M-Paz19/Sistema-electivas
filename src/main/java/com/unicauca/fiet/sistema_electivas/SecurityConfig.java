package com.unicauca.fiet.sistema_electivas;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // ✅ forma nueva
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // de momento todo abierto
                )
                .httpBasic(Customizer.withDefaults()); // opcional: habilita basic auth

        return http.build();
    }
}


package com.example.esm_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure PasswordEncoder using BCrypt
     * BCrypt automatically adds salt and securely hashes passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure AuthenticationManager
     * Required for authenticating users during login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configure Security Filter Chain
     * Allow public access to endpoints: register, login, swagger, actuator
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
                .formLogin(form -> form.disable()) // Disable form login (this is a REST API)
                .httpBasic(basic -> basic.disable()) // Disable HTTP Basic authentication
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests for development
                );

        return http.build();
    }
}

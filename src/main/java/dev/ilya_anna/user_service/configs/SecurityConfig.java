package dev.ilya_anna.user_service.configs;

import dev.ilya_anna.user_service.authorizers.DaoUserAuthorizer;
import dev.ilya_anna.user_service.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private DaoUserAuthorizer daoUserAuthorizer;
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET,"/api/v1/user/all-info/{userId}").access(daoUserAuthorizer)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user/{userId}", "/api/v1/user-settings/{userId}").access(daoUserAuthorizer)
                        .requestMatchers(HttpMethod.POST, "/api/v1/user-avatars/{userId}").access(daoUserAuthorizer)
                        .anyRequest().permitAll()
                )
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();


    }
}
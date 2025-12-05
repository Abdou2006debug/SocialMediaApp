package com.example.whatsappclone.Securityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Securityconfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.
                csrf(AbstractHttpConfigurer::disable).
                oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults())).
                authorizeHttpRequests(authorization -> authorization.requestMatchers("api/v1/users/accounts/register","/v3/api-docs/**","/swagger-ui/**", "/swagger-ui.html").permitAll().requestMatchers("/ws").authenticated().anyRequest().permitAll());
        return http.build();
    }

}

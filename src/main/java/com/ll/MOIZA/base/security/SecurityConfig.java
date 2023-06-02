package com.ll.MOIZA.base.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/memberLogin").permitAll()
                                .requestMatchers("/*.css").permitAll()
                                .requestMatchers("/img/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/**").authenticated()
                )
                .oauth2Login(
                        oauth2Login -> oauth2Login.loginPage("/login")
                                .successHandler((request, response, authentication) ->
                                        response.sendRedirect("/groups")
                                )
                )
                .logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessHandler((request, response, authentication) -> {
                                        response.sendRedirect("/login");
                                   }
                                )
                                .invalidateHttpSession(true)
                )
                .exceptionHandling(
                        exception -> exception.accessDeniedPage("/login")
                );

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.oprm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(request -> {
                                        var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                                        corsConfiguration.setAllowedOriginPatterns(java.util.List.of("*"));
                                        corsConfiguration
                                                        .setAllowedMethods(java.util.List.of("GET", "POST", "PUT",
                                                                        "DELETE", "PATCH", "OPTIONS"));
                                        corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
                                        corsConfiguration.setAllowCredentials(true);
                                        return corsConfiguration;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login", "/register", "/forgot-password",
                                                                "/reset-password", "/css/**", "/js/**", "/error")
                                                .permitAll()
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/professor/**").hasRole("PROFESSOR")
                                                .requestMatchers(HttpMethod.PUT,
                                                                "/api/professors/*/accept-mentorship/**")
                                                .hasAnyRole("PROFESSOR", "ADMIN")
                                                .requestMatchers("/projects/submit", "/projects/my",
                                                                "/mentors/suggestions")
                                                .hasRole("STUDENT")
                                                .requestMatchers("/dashboard", "/projects/**", "/resources/**",
                                                                "/notifications/**",
                                                                "/api/projects/**", "/api/resources/**")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .deleteCookies("jwt")
                                                .permitAll())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
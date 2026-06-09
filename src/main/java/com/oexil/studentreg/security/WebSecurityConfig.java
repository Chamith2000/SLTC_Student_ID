package com.oexil.studentreg.security;

import com.oexil.studentreg.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;

    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // Session-based authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

                // Define access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/vendors/**", "/files/**").permitAll() // Allow public access to login and static resources
                        .requestMatchers("/public/student/**").permitAll() // Allow public access to student
                        .anyRequest().authenticated() // Protect all other URLs
                )

                // Configure login
                .formLogin(form -> form
                        .loginPage("/auth/login") // Custom login page
                        .loginProcessingUrl("/do-login") // URL to submit login form
                        .defaultSuccessUrl("/", true) // Redirect after successful login
                        .failureUrl("/auth/login?error") // Redirect on login failure
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // Custom logout URL
                        .logoutSuccessUrl("/auth/login?logout") // Redirect after logout
                        .invalidateHttpSession(true) // Invalidate session
                        .deleteCookies("JSESSIONID") // Delete session cookie
                        .permitAll()
                );
        return http.build();
    }
}

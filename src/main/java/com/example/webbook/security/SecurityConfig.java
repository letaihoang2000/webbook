package com.example.webbook.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Special handler for Remember Me authentication
     * This handler does NOT redirect - it lets Spring Security continue with the original request
     */
    @Bean
    public AuthenticationSuccessHandler rememberMeAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                // Spring Security will then check authorization and redirect to 403 if needed
                clearAuthenticationAttributes(request);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**").permitAll()
                        .requestMatchers("/login", "/register", "/", "/403").permitAll()

                        // Admin only access - ORDER MATTERS!
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/category/**").hasRole("ADMIN")
                        .requestMatchers("/author/**").hasRole("ADMIN")
                        .requestMatchers("/book/**").hasRole("ADMIN")

                        // Customer access (both USER and ADMIN can access customer pages)
                        .requestMatchers("/customer/**").hasAnyRole("USER", "ADMIN")

                        // Any other request needs authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("mySecretKey")
                        .tokenValiditySeconds(86400)
                        .userDetailsService(customUserDetailsService)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me-cookie")
                        .authenticationSuccessHandler(rememberMeAuthenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me-cookie")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403")
                )
                .userDetailsService(customUserDetailsService)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/admin/**", "/category/**", "/author/**", "/book/**", "/logout")
                );

        return http.build();
    }
}
package org.example.config;

import org.example.service.CoachUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CoachUserDetailsService userDetailsService;

    public SecurityConfig(CoachUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Use a DaoAuthenticationProvider so Spring Security knows to
     * load users from your coaches table and verify with BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Main security filter chain: apply our auth provider,
     * require authentication on every request, and enable form-login.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authenticationProvider(authenticationProvider())
          .authorizeHttpRequests(auth -> auth
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/login")      // custom page
              .permitAll()
          )
          .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Expose the AuthenticationManager, if you need it elsewhere.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * BCrypt encoder for hashing and checking coach passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

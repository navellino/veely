package com.veely.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.veely.entity.Employee;
import com.veely.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disabilito HSTS per semplificare lo sviluppo in dev
            .headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts.disable()))

            // Configuro il login form standard di Spring Security
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )

            // Abilito il logout su /logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // Rendo tutte le rotte aperte
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            // CSRF abilitato di default; se vuoi disabilitarlo in dev:
            // .csrf(csrf -> csrf.disable())
            ;

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
}


/**
 * Sicurezza HTTP + autenticazione via:
 *  • Form-login (tabella employees)
 *  • OAuth2 Google (opzionale)
 */
/*
@Configuration
@EnableWebSecurity
@EnableMethodSecurity       // abilita @PreAuthorize, @Secured, ecc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final EmployeeRepository employeeRepository;

    // === Password encoder ===
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // === UserDetailsService: carica l’utente dal DB ===
    @Bean
    public UserDetailsService userDetailsService() {
    	return username -> {
            Employee emp = employeeRepository.findByEmail(username)
            		.orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

            return User.builder()
            		.username(emp.getEmail())
                    .password(emp.getPassword())
                    .roles(emp.getRole().name())
                    .build();
        };
    }

    // === HTTP security filter chain ===
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Risorse pubbliche
        	.headers(headers -> headers.httpStrictTransportSecurity(hsts -> hsts.disable()))
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/oauth2/**").permitAll()
                    .requestMatchers("/settings/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // Form-login classico
            .formLogin(form -> form
                .loginPage("/login")               // Thymeleaf custom page
                .defaultSuccessUrl("/", true)
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )

            // Google OAuth2 (se configurato)
            .oauth2Login(oauth -> oauth
                .loginPage("/login")               // stessa pagina di login
                .defaultSuccessUrl("/", true)
            );

        return http.build();
    }
 
}
*/

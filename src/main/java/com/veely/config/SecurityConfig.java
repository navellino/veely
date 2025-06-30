package com.veely.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
        return (String username) -> {
            Employee emp = employeeRepository.findByEmail(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Utente non trovato: " + username));

            return User.builder()
                       .username(emp.getEmail())
                       .password(emp.getPassword())              // già cifrata BCrypt
                       .roles(emp.getRole().name())              // enum → “ADMIN”, “MANAGER”, “USER”
                       .build();
        };
    }

    // === HTTP security filter chain ===
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Risorse pubbliche
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**","/js/**","/images/**",
                                 "/login","/oauth2/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/fleet/**").hasAnyRole("ADMIN","MANAGER")
                .anyRequest().authenticated()
            )

            // Form-login classico
            .formLogin(form -> form
                .loginPage("/login")               // Thymeleaf custom page
                .defaultSuccessUrl("/", true)
                .permitAll()
            )

            // Logout
            .logout(out -> out
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

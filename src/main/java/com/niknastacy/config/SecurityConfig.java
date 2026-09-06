package com.niknastacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        .requestMatchers("/", "/projects/list", "/projects/{id}", "/users/list", "/users/{id}").permitAll()
                        .requestMatchers("/users/register", "/users/login").anonymous()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/projects/list", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/projects/list")
                        .permitAll()
                );

        return http.build();
    }
}

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
        return new BCryptPasswordEncoder(); // Для шифрования паролей
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Статика
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        // Публичные страницы
                        .requestMatchers("/", "/projects/list", "/projects/{id}", "/users/list", "/users/{id}").permitAll()
                        .requestMatchers("/users/register", "/users/login").anonymous()
                        // Всё остальное требует авторизации (создание проектов, профиль)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login") // Наша кастомная страница логина
                        .loginProcessingUrl("/users/login") // Куда POST-запрос отправляет форму
                        .usernameParameter("email") // Используем email вместо логина
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

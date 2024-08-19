package com.server.hearoad.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/api/users/register/**", "/api/mypage/info").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(withDefaults()) // 기본 로그인 폼 사용
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

}

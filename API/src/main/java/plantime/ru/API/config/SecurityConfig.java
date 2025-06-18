package plantime.ru.API.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности для API PlanTime.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Включаем CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/employee/**").permitAll()
                        .requestMatchers("/api/employee/departments/**").permitAll()
                        .requestMatchers("/api/employee/statuses/**").permitAll()
                        .requestMatchers("/api/employee/genders/**").permitAll()
                        .requestMatchers("/api/employee/permissions/**").permitAll()
                        .requestMatchers("/api/employee/posts/**").permitAll()
                        .requestMatchers("/static/employee/**").permitAll()
                        .requestMatchers("/employee/images/**").permitAll()
                        .requestMatchers("/api/duty-schedule/**").permitAll()
                        .requestMatchers("/api/organizations/**").permitAll()
                        .requestMatchers("/api/task/types/**").permitAll()
                        .requestMatchers("/api/task/statuses/**").permitAll()
                        .requestMatchers("/api/task/recurrences/**").permitAll()
                        .requestMatchers("/api/project/statuses/**").permitAll()
                        .requestMatchers("/api/payment/statuses/**").permitAll()
                        .requestMatchers("/api/software/**").permitAll()
                        .requestMatchers("/api/customers/**").permitAll()
                        .requestMatchers("/api/tasks/**").permitAll()
                        .requestMatchers("/api/tasks/**").permitAll()
                        .requestMatchers("/api/list-of-software/**").permitAll()
                        .requestMatchers("/api/task-tree/**").permitAll()
                        .requestMatchers("/api/project/**").permitAll()
                        .requestMatchers("/api/services/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Настройка CORS для Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3001"));  // Разрешенный origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // Разрешенные методы
        configuration.setAllowedHeaders(List.of("*"));  // Разрешенные заголовки
        configuration.setAllowCredentials(true);  // Разрешить куки и авторизацию

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Применить ко всем путям
        return source;
    }
}
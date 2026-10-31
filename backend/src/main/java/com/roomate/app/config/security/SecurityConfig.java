package com.roomate.app.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
// @Profile("!test")
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://127.0.0.1:8085}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter)
            throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new ForwardedProtoCsrfTokenRepository(csrfTokenRepository))
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/user/login", "/user/register", "/user/verify"))
                .addFilterAfter((request, response, chain) -> {
                    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrfToken != null) {
                        ((HttpServletResponse) response).setHeader("X-CSRF-TOKEN", csrfToken.getToken());
                    }
                    chain.doFilter(request, response);
                }, CsrfFilter.class)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/user/register", "/user/login", "/user/status", "/user/verify").permitAll()
                        .requestMatchers("/public_resource").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/**").authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/**").hasAnyRole("HEAD_ROOMMATE", "ADMIN")
                        .requestMatchers("/assistant/**").hasAnyRole("ASSISTANT_ROOMMATE", "HEAD_ROOMMATE", "ADMIN")
                        .requestMatchers("/view/**")
                        .hasAnyRole("ROOMMATE", "ASSISTANT_ROOMMATE", "HEAD_ROOMMATE", "ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // EFFECTS : Configures cor policy allowing frontend to access api's
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders("*")
                        .exposedHeaders("X-CSRF-TOKEN")
                        .maxAge(3600);
            }
        };
    }

    private static final class ForwardedProtoCsrfTokenRepository implements CsrfTokenRepository {
        private final CookieCsrfTokenRepository delegate;

        private ForwardedProtoCsrfTokenRepository(CookieCsrfTokenRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public CsrfToken generateToken(HttpServletRequest request) {
            return delegate.generateToken(request);
        }

        @Override
        public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
            delegate.saveToken(token, request, new HttpServletResponseWrapper(response) {
                @Override
                public void addCookie(Cookie cookie) {
                    if (!"XSRF-TOKEN".equals(cookie.getName())) {
                        super.addCookie(cookie);
                        return;
                    }
                    boolean https = RequestHttps.isHttps(request);
                    String value = cookie.getValue() == null ? "" : cookie.getValue();
                    String path = cookie.getPath() == null || cookie.getPath().isEmpty() ? "/" : cookie.getPath();
                    ResponseCookie rewritten = ResponseCookie.from(cookie.getName(), value)
                            .path(path)
                            .maxAge(cookie.getMaxAge())
                            .httpOnly(cookie.isHttpOnly())
                            .secure(https)
                            .sameSite(https ? "None" : "Lax")
                            .build();
                    addHeader(HttpHeaders.SET_COOKIE, rewritten.toString());
                }
            });
        }

        @Override
        public CsrfToken loadToken(HttpServletRequest request) {
            return delegate.loadToken(request);
        }
    }
}

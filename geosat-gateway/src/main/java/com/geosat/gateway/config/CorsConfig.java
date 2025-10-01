package com.geosat.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed-origins:http://localhost:8080,http://localhost:5173,http://127.0.0.1:8080}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    
    @Value("${cors.max-age:3600}")
    private Long maxAge;
    
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        c.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(Arrays.asList(
            "X-Correlation-ID",
            "Retry-After",
            "Content-Type"
        ));
        c.setAllowCredentials(true);
        c.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return new CorsFilter(source);
    }
}

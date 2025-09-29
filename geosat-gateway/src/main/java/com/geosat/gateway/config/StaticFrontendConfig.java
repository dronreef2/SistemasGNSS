package com.geosat.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Expõe o frontend estático (diretório externo "frontend/web") via /app/* sem precisar mover arquivos.
 * Em produção, ideal empacotar assets no jar ou servir via CDN.
 */
@Configuration
public class StaticFrontendConfig implements WebMvcConfigurer {

    private Path frontendDir(){
        // Caminho relativo ao root do projeto (codespace/container). Ajustável via env no futuro.
        return Paths.get("frontend", "web");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = frontendDir().toAbsolutePath();
        String location = dir.toUri().toString();
        registry.addResourceHandler("/app/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/app").setViewName("forward:/app/index.html");
    }
}

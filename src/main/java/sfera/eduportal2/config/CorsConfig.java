package sfera.eduportal2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Frontend manzillaringizni shu yerga yozasiz. 
        // Masalan: React odatda 3000, Vite (Vue/React) 5173 portida ishlaydi.
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173")); 
        
        // Agar Hamma manzillardan kirishga ruxsat bermoqchi bo'lsangiz (faqat test uchun tavsiya qilinadi):
        // config.setAllowedOriginPatterns(Arrays.asList("*")); 

        // Ruxsat etilgan HTTP metodlar
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Ruxsat etilgan Header'lar (Ayniqsa JWT Authorization uchun juda muhim)
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        
        // Brauzer orqali xavfsizlik tokenlari va cookie'larni jo'natishga ruxsat berish
        config.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Barcha API yo'llari uchun yuqoridagi qoidalarni tatbiq qilish
        source.registerCorsConfiguration("/**", config); 
        
        return new CorsFilter(source);
    }
}
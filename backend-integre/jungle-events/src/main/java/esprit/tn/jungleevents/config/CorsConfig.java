package esprit.tn.jungleevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
      return new WebMvcConfigurer() {


        public void addCorsRegistry(CorsRegistry registry) {
          registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*");
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
          // Chemin absolu — même base que le controller
          String uploadPath = new File("uploads/").getAbsolutePath() + "/";

          registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath);
        }
      };
    }
}

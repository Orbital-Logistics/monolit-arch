package org.orbitalLogistic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orbital Logistic API")
                        .version("3.0")
                        .description("Orbital Logistic API HighloadSystem laboratory 1")
                );
    }
}

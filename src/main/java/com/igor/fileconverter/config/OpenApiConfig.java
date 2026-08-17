package com.igor.fileconverter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileConverterOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FileConverter API")
                        .description("API para upload e conversao local de arquivos")
                        .version("v1"));
    }
}

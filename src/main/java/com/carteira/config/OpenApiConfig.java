package com.carteira.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Carteira API")
                        .description("API REST de carteira digital — transferências de saldo entre carteiras identificadas por CPF.")
                        .version("v1"))
                .tags(List.of(
                        new Tag().name("wallets").description("Criação e consulta de carteiras"),
                        new Tag().name("transactions").description("Transferências entre carteiras")
                ));
    }
}

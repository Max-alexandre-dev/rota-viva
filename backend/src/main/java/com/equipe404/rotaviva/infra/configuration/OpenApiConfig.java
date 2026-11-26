package com.equipe404.rotaviva.infra.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Rota Viva API",
        version = "1.0.0",
        description = "API de gestão de recursos e transferências entre centros de apoio"
))
public class OpenApiConfig {
}

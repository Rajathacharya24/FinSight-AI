package com.finsight.authservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("FinSight AI — Auth Service API")
                .description("""
                        Authentication and authorization service for the FinSight AI platform.

                        ## Authentication Flow
                        1. **Register** — `POST /api/v1/auth/register` — Create a new account.
                        2. **Login** — `POST /api/v1/auth/login` — Obtain an access token (24h) and a refresh token (7 days).
                        3. **Use the token** — Add `Authorization: Bearer <accessToken>` to protected requests.
                        4. **Refresh** — `POST /api/v1/auth/refresh` — Exchange a refresh token for a new access token.

                        ## Roles
                        | Role | Description |
                        |------|-------------|
                        | `ROLE_USER` | Standard user — default on registration |
                        | `ROLE_ANALYST` | Elevated access for analysts |
                        | `ROLE_ADMIN` | Full administrative access |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("FinSight AI Team")
                        .email("support@finsight.ai")
                )
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                );
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste the JWT access token obtained from POST /api/v1/auth/login");
    }
}

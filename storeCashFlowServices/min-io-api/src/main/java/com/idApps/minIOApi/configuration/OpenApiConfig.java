package com.idApps.minIOApi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${keycloak.realm-url}")
    private String realmUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Flow Space Swagger API")
                .version("1.0")
                .description("Documentation de l'API sécurisée par Keycloak"))
            .components(new Components()
                .addSecuritySchemes("keycloak", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .description("Keycloak Login")
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl(this.realmUrl + "/protocol/openid-connect/auth")
                            .tokenUrl(this.realmUrl + "/protocol/openid-connect/token")
                            .scopes(new Scopes()
                                .addString("openid", "OpenID Connect scope")
                                .addString("profile", "User profile")
                                .addString("email", "User email")
                            )
                        )
                    )
                )
            );
    }
}
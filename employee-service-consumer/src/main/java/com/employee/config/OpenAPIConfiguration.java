package com.employee.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * API configuration for OpenApi.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Crud",
                version = "1.0.0",
                contact = @Contact(
                        name = "Marcos Luna",
                        email = "josemarcosluna9@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                description = "Basic Crud of Employees"
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Production"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfiguration {}

package com.ecom.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI ecommerceOpenAPI() {
    Server devServer = new Server()
        .url("http://localhost:8080")
        .description("Development server");

    Contact contact = new Contact()
        .name("E-Commerce API Team")
        .email("support@ecom-api.com");

    License license = new License()
        .name("Private")
        .url("https://ecom-api.com/license");

    Info info = new Info()
        .title("E-Commerce API")
        .version("0.1.0")
        .description("API documentation for the E-Commerce API")
        .contact(contact)
        .license(license);

    return new OpenAPI()
        .info(info)
        .servers(List.of(devServer));
  }
}

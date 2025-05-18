package dev.ilya_anna.user_service.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
  info = @Info(
    title = "User Service API",
    version = "1.0",
    description = "API for User Service",
    contact = @Contact(
      name = "Anna",
      email = "anna.gudkova.rus@mail.ru",
      url = "https://github.com/AnnaYellowCat"
    )
  )
)
@Configuration
public class OpenApiConfig {
  
}

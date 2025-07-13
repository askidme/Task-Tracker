package com.codergm.tasktracker.config

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun publicApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("tasks")
        .pathsToMatch("/api/tasks/**")
        .build()

    @Bean
    fun apiInfo(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Task Tracker API")
                .description("API documentation for the Task Tracker service")
                .version("1.0.0")
        )
}

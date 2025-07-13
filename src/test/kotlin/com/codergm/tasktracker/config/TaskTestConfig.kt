package com.codergm.tasktracker.config

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class TaskTestConfig {

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun clearCache() {
        cacheManager.cacheNames.forEach { name ->
            cacheManager.getCache(name)?.also {
                it.clear()
                println("✅ Cleared cache: $name")
            }
        }
    }

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("taskdb")
            withUsername("testuser")
            withPassword("testpass")
        }.also {
            it.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
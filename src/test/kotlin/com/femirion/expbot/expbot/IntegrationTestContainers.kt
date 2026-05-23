package com.femirion.expbot.expbot

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

object IntegrationTestContainers {
    private val postgres = PostgreSQLContainer("postgres:16-alpine")
        .withDatabaseName("expbot_test")
        .withUsername("expbot")
        .withPassword("expbot")

    fun configurePostgres(registry: DynamicPropertyRegistry) {
        postgres.start()
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }
}

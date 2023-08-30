package com.github.egrmdev.banking

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

open class IntegrationTestBase {
    companion object {
        private const val POSTGRES_IMAGE = "postgres:15"

        private val postgresContainer =
            PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
                .waitingFor(Wait.defaultWaitStrategy())
                .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }
}
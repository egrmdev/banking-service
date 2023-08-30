package com.github.egrmdev.banking

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ApplicationTest : IntegrationTestBase() {
    @Test
    fun `application should start`() {

    }
}
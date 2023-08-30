package com.github.egrmdev.banking.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.egrmdev.banking.config.ObjectMapperConfiguration.Companion.configureObjectMapper
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

@ExtendWith(MockKExtension::class)
internal abstract class MvcTestBase {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    protected lateinit var objectMapper: ObjectMapper

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        // objectMapper needs to be in sync with updates on ObjectMapperConfiguration
        objectMapper = ObjectMapper()
        configureObjectMapper(objectMapper)
    }
}
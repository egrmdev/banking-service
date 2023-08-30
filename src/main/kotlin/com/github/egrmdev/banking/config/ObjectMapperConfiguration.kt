package com.github.egrmdev.banking.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class ObjectMapperConfiguration(
    objectMapper: ObjectMapper
) {

    init {
        configureObjectMapper(objectMapper)
    }

    companion object {
        fun configureObjectMapper(objectMapper: ObjectMapper) {
            with(objectMapper) {
                registerModule(JavaTimeModule())
                registerModule(KotlinModule.Builder().build())
                setTimeZone(TimeZone.getDefault())

                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            }
        }
    }
}
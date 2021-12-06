package com.cmartin.learn

import arrow.core.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExceptionManagementTest {

    @Test
    fun shouldReturnIntForValidCountry() {
        val country = Model.Country("es", "Spain")
        val result: Either<String, Long> = ValidationModule.save(country)
        assertTrue(result.isRight())
        result.map { assertEquals(1, it) }
    }

    @Test
    fun shouldReturnErrorForInvalidCountry() {
        val country = Model.Country("", "Spain")
        val result: Either<String, Long> = ValidationModule.save(country)
        assertTrue(result.isLeft())
        result.mapLeft {
            assertEquals("Empty code for: $country", it)
        }
    }


}
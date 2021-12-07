package com.cmartin.learn

import arrow.core.Either
import com.cmartin.learn.ExceptionManagementModule.DummyService
import com.cmartin.learn.ExceptionManagementModule.RepositoryError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExceptionManagementTest {

    @Test
    fun shouldReturnIntForValidCountry() {
        val country = Model.Country("es", "Spain")
        val result: Either<RepositoryError, Long> = DummyService.save(country)
        assertTrue(result.isRight())
        result.map { assertEquals(1, it) }
    }

    @Test
    fun shouldReturnErrorForInvalidCountry() {
        val country = Model.Country("", "Spain")
        val result: Either<RepositoryError, Long> = DummyService.save(country)
        assertTrue(result.isLeft())
        result.mapLeft {
            val message = "${ExceptionManagementModule.INVALID_DATA_MSG}: $country"
            assertEquals(RepositoryError.DataIntegrityError(message), it)
        }
    }

}
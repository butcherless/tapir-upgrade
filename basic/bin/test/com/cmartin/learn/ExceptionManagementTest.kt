package com.cmartin.learn

import arrow.core.Either
import com.cmartin.learn.ExceptionManagementModule.DummyService
import com.cmartin.learn.ExceptionManagementModule.RepositoryError
import com.cmartin.learn.Model.Code
import com.cmartin.learn.Model.Country
import com.cmartin.learn.Model.Name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExceptionManagementTest {

    @Test
    fun `should return Int for valid country`() {
        val country = Country(Code("es"), Name("Spain"))
        val result: Either<RepositoryError, Long> = DummyService.save(country)
        assertTrue(result.isRight())
        result.map { assertEquals(1, it) }
    }

    @Test
    fun `should return error for invalid country`() {
        val country = Country(Code(""), Name("Spain"))
        val result: Either<RepositoryError, Long> = DummyService.save(country)
        assertTrue(result.isLeft())
        result.mapLeft {
            val message = "${ExceptionManagementModule.INVALID_DATA_MSG}: $country"
            assertEquals(RepositoryError.DataIntegrityError(message), it)
        }
    }
}

package com.cmartin.learn

import arrow.core.*
import com.cmartin.learn.Model.Country
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTest {

    @Test
    fun shouldReturnRight() {
        val result = MyLib.validateNonEmptyText("not empty")
        assertTrue(result.isRight(), "must be Either.Right")
    }

    @Test
    fun shouldReturnLeft() {
        val result = MyLib.validateNonEmptyText("")
        assertTrue(result.isLeft(), "must be Either.Left")
    }

    @Test
    fun shouldReturnValid() {
        val result = MyLib.validateTextLength("abc")
        assertTrue(result.isValid, "must be Validated.Valid")
    }

    @Test
    fun shouldReturnInvalid() {
        val result = MyLib.validateTextLength("12345678xx")
        assertTrue(result.isInvalid, "must be Validated.Invalid")
    }

    @Test
    fun shouldReturnX() {
        val result1 = MyLib.validationOne("123456789")
        println("WIP:" + result1.toString())
        val result2 = MyLib.validationTwo("123456789")
        println("WIP:" + result2.toString())
        val result3 = MyLib.validationNel("123456789")
        println("WIP:" + result3.toString())
    }

    @Test
    fun shouldReturnIntForValidCountry() {
        val country = Country("es", "Spain")
        val result: Either<String, Long> = MyLib.save(country)
        assertTrue(result.isRight())
        result.map { assertEquals(1, it) }
    }

    @Test
    fun shouldReturnErrorForInvalidCountry() {
        val country = Country("", "Spain")
        val result: Either<String, Long> = MyLib.save(country)
        assertTrue(result.isLeft())
        result.mapLeft { assertEquals("Empty code for: $country", it) }
    }
}

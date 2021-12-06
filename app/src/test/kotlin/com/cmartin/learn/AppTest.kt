package com.cmartin.learn

import arrow.core.Either
import arrow.core.nonEmptyListOf
import com.cmartin.learn.Model.Country
import com.cmartin.learn.MyLib.ValidationError.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTest {

    @Test
    fun `should validate text`() {
        val text = "abcd1234"
        val result = MyLib.validateText(text)
        assertTrue(result.isRight(), "must be Right")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text, length`() {
        val text = "1234567890"
        val result = MyLib.validateText(text)
        assertTrue(result.isLeft(), "must be Valid")
        result.mapLeft {
            assertEquals(
                nonEmptyListOf(InvalidTextLength(text.length)), it
            )
        }
    }

    @Test
    fun `should fail to validate text, length and chars`() {
        val text = "1234@@abcd"
        val result = MyLib.validateText(text)
        assertTrue(result.isLeft(), "must be Valid")
        result.mapLeft {
            assertEquals(
                nonEmptyListOf(InvalidTextLength(text.length), InvalidTextChars(text)), it
            )
        }
    }

    /*
       S I N G LE   V A L I D A T O R S
    */

    @Test
    fun `should validate non-empty text`() {
        val text = "non-empty text"
        val result = MyLib.validateNonEmptyText(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate non-empty text`() {
        val text = " "
        val result = MyLib.validateNonEmptyText(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }

    @Test
    fun `should validate text length`() {
        val text = "12345678"
        val result = MyLib.validateTextLength(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text length`() {
        val text = "1234567890"
        val result = MyLib.validateTextLength(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextLength(text.length)), it)
        }
    }

    @Test
    fun `should validate text chars`() {
        val text = "abcd1234"
        val result = MyLib.validateTextChars(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text chars`() {
        val text = "abc@_123"
        val result = MyLib.validateTextChars(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextChars(text)), it)
        }
    }

    @Test
    fun `should fail to validate text, empty`() {
        val text = ""
        val result = MyLib.validateText(text)
        assertTrue(result.isLeft(), "must be Valid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }


    @Test
    fun shouldReturnRight() {
        val result = MyLib.validateEitherNonEmptyText("not empty")
        assertTrue(result.isRight(), "must be Either.Right")
    }

    @Test
    fun shouldReturnLeft() {
        val result = MyLib.validateEitherNonEmptyText("")
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
        result.mapLeft {
            assertEquals("Empty code for: $country", it)
        }
    }
}

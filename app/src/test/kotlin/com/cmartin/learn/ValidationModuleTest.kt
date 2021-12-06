package com.cmartin.learn

import arrow.core.nonEmptyListOf
import com.cmartin.learn.ValidationModule.ValidationError.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationModuleTest {

    @Test
    fun `should validate text`() {
        val text = "abcd1234"
        val result = ValidationModule.validateText(text)
        assertTrue(result.isRight(), "must be Right")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text, length`() {
        val text = "1234567890"
        val result = ValidationModule.validateText(text)
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
        val result = ValidationModule.validateText(text)
        assertTrue(result.isLeft(), "must be Valid")
        result.mapLeft {
            assertEquals(
                nonEmptyListOf(InvalidTextLength(text.length), InvalidTextChars(text)), it
            )
        }
    }

    /*
       S I N G L E   V A L I D A T O R S
    */

    @Test
    fun `should validate non-empty text`() {
        val text = "non-empty text"
        val result = ValidationModule.validateNonEmptyText(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate non-empty text`() {
        val text = " "
        val result = ValidationModule.validateNonEmptyText(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }

    @Test
    fun `should validate text length`() {
        val text = "12345678"
        val result = ValidationModule.validateTextLength(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text length`() {
        val text = "1234567890"
        val result = ValidationModule.validateTextLength(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextLength(text.length)), it)
        }
    }

    @Test
    fun `should validate text chars`() {
        val text = "abcd1234"
        val result = ValidationModule.validateTextChars(text)
        assertTrue(result.isValid, "must be Valid")
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text chars`() {
        val text = "abc@_123"
        val result = ValidationModule.validateTextChars(text)
        assertTrue(result.isInvalid, "must be Invalid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextChars(text)), it)
        }
    }

    @Test
    fun `should fail to validate text, empty`() {
        val text = ""
        val result = ValidationModule.validateText(text)
        assertTrue(result.isLeft(), "must be Valid")
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }


    @Test
    fun shouldReturnRight() {
        val result = ValidationModule.validateEitherNonEmptyText("not empty")
        assertTrue(result.isRight(), "must be Either.Right")
    }

    @Test
    fun shouldReturnLeft() {
        val result = ValidationModule.validateEitherNonEmptyText("")
        assertTrue(result.isLeft(), "must be Either.Left")
    }

    @Test
    fun shouldReturnValid() {
        val result = ValidationModule.validateTextLength("abc")
        assertTrue(result.isValid, "must be Validated.Valid")
    }

    @Test
    fun shouldReturnInvalid() {
        val result = ValidationModule.validateTextLength("12345678xx")
        assertTrue(result.isInvalid, "must be Validated.Invalid")
    }

}

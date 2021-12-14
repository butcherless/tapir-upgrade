package com.cmartin.learn

import arrow.core.nonEmptyListOf
import com.cmartin.learn.ValidationModule.ValidationError.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val MUST_BE_RIGHT = "must be Right"
private const val MUST_BE_LEFT = "must be Left"
private const val MUST_BE_VALID = "must be Valid"
private const val MUST_BE_INVALID = "must be Invalid"

class ValidationModuleTest {

    @Test
    fun `should validate text`() {
        val text = "abcd1234"
        val result = ValidationModule.validateText(text)
        assertTrue(result.isRight(), MUST_BE_RIGHT)
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text, empty`() {
        val text = ""
        val result = ValidationModule.validateText(text)
        assertTrue(result.isLeft(), MUST_BE_LEFT)
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }

    @Test
    fun `should fail to validate text, length`() {
        val text = "1234567890"
        val result = ValidationModule.validateText(text)
        assertTrue(result.isLeft(), MUST_BE_LEFT)
        result.mapLeft { assertEquals(nonEmptyListOf(InvalidTextLength(text.length)), it) }
    }

    @Test
    fun `should fail to validate text, length and chars`() {
        val text = "1234@@abcd"
        val result = ValidationModule.validateText(text)
        assertTrue(result.isLeft(), MUST_BE_LEFT)
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextLength(text.length), InvalidTextChars(text)), it)
            println("errors:  $it")
        }
    }

    /*
       S I N G L E   V A L I D A T O R S
    */

    @Test
    fun `should validate non-empty text`() {
        val text = "non-empty text"
        val result = ValidationModule.validateNonEmptyText(text)
        assertTrue(result.isValid, MUST_BE_VALID)
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate non-empty text`() {
        val text = " "
        val result = ValidationModule.validateNonEmptyText(text)
        assertTrue(result.isInvalid, MUST_BE_INVALID)
        result.mapLeft {
            assertEquals(nonEmptyListOf(EmptyText(text.length)), it)
        }
    }

    @Test
    fun `should validate text length`() {
        val text = "12345678"
        val result = ValidationModule.validateTextLength(text)
        assertTrue(result.isValid, MUST_BE_VALID)
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text length`() {
        val text = "1234567890"
        val result = ValidationModule.validateTextLength(text)
        assertTrue(result.isInvalid, MUST_BE_INVALID)
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextLength(text.length)), it)
        }
    }

    @Test
    fun `should validate text chars`() {
        val text = "abcd1234"
        val result = ValidationModule.validateTextChars(text)
        assertTrue(result.isValid, MUST_BE_VALID)
        result.map { assertEquals(text, it) }
    }

    @Test
    fun `should fail to validate text chars`() {
        val text = "abc@_123"
        val result = ValidationModule.validateTextChars(text)
        assertTrue(result.isInvalid, MUST_BE_INVALID)
        result.mapLeft {
            assertEquals(nonEmptyListOf(InvalidTextChars(text)), it)
        }
    }

}

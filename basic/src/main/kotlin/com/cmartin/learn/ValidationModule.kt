package com.cmartin.learn

import arrow.core.*
import com.cmartin.learn.ValidationModule.ValidationError.*

object ValidationModule {
    private const val MAX_TEXT_LENGTH = 8

    /** Error Model - ADT
     */
    sealed class ValidationError {
        data class EmptyText(val value: Int) : ValidationError()
        data class InvalidTextLength(val value: Int) : ValidationError()
        data class InvalidTextChars(val message: String) : ValidationError()
    }

    /** Validate text with error accumulation. Use cases:
     *  a) validation succeed: Either<_, A> - A validated
     *  b) validation failed:  Either<Nel<E>, _> - NonEmptyList<E> errors
     */
    fun validateText(text: String): Either<Nel<ValidationError>, String> =
        validateNonEmptyText(text)
            .zip(
                validateTextLength(text),
                validateTextChars(text)
            ) { _, _, _ -> text }
            .toEither()

    /** Validate a non-blank text
     *  a) succeed: the text
     *  b) failed: error non-empty-list
     */
    fun validateNonEmptyText(text: String): ValidatedNel<ValidationError, String> =
        if (text.isBlank()) EmptyText(text.length).invalidNel()
        else text.validNel()

    /** Validate a non-blank text
     *  a) succeed: the text
     *  b) failed: error non-empty-list
     */
    fun validateTextLength(text: String): ValidatedNel<ValidationError, String> =
        if (text.length > MAX_TEXT_LENGTH)
            InvalidTextLength(text.length).invalidNel()
        else text.validNel()

    fun validateTextChars(text: String): ValidatedNel<ValidationError, String> =
        if (text.all { it.isLetterOrDigit() }) text.validNel()
        else InvalidTextChars(text).invalidNel()

}

package com.cmartin.learn

import arrow.core.*


object ValidationModule {
    const val EMPTY_TEXT = "empty text"
    const val INVALID_TEXT_LENGTH = "Invalid length"
    const val INVALID_TEXT_CHARS = "Invalid characters"

    sealed class ValidationError(val message: String) {
        data class EmptyText(val value: Int) : ValidationError(EMPTY_TEXT)
        data class InvalidTextLength(val value: Int) : ValidationError(INVALID_TEXT_LENGTH)
        data class InvalidTextChars(val value: String) : ValidationError(INVALID_TEXT_CHARS)
    }

    fun validateText(text: String): Either<Nel<ValidationError>, String> {
        return validateNonEmptyText(text)
            .zip(
                validateTextLength(text),
                validateTextChars(text)
            ) { _, _, _ -> text }
            .toEither()
    }

    fun validateNonEmptyText(text: String): ValidatedNel<ValidationError, String> {
        return if (text.isBlank()) ValidationError.EmptyText(text.length).invalidNel()
        else text.validNel()
    }

    fun validateTextLength(text: String): ValidatedNel<ValidationError, String> {
        return if (text.length > 8) ValidationError.InvalidTextLength(text.length).invalidNel()
        else text.validNel()
    }

    fun validateTextChars(text: String): ValidatedNel<ValidationError, String> {
        return if (text.all { it.isLetterOrDigit() }) text.validNel()
        else ValidationError.InvalidTextChars(text).invalidNel()
    }


    fun validateEitherNonEmptyText(text: String): Either<String, String> {
        return if (text.isBlank()) Either.Left(EMPTY_TEXT) else Either.Right(text)
    }

}

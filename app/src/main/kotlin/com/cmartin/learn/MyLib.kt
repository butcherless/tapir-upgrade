package com.cmartin.learn

import arrow.core.*
import com.cmartin.learn.Model.Country
import java.sql.SQLIntegrityConstraintViolationException


object MyLib {
    const val EMPTY_TEXT = "empty text"
    const val INVALID_TEXT_LENGTH = "Invalid length"
    const val INVALID_TEXT_CHARS = "Invalid characters"

    fun validateText(text: String): Either<Nel<String>, String> {
        return validateNonEmptyText(text)
            .zip(
                validateTextLength(text), validateTextChars(text)
            ) { _, _, _ -> text }
            .toEither()
    }

    fun validateNonEmptyText(text: String): ValidatedNel<String, String> {
        return if (text.isBlank()) EMPTY_TEXT.invalidNel()
        else text.validNel()
    }

    fun validateTextLength(text: String): ValidatedNel<String, String> {
        return if (text.length > 8) INVALID_TEXT_LENGTH.invalidNel() else text.validNel()
    }


    fun validateEitherNonEmptyText(text: String): Either<String, String> {
        return if (text.isBlank()) Either.Left(EMPTY_TEXT) else Either.Right(text)
    }

    fun validateTextChars(text: String): ValidatedNel<String, String> {
        return if (text.all { it.isLetterOrDigit() }) text.validNel()
        else INVALID_TEXT_CHARS.invalidNel()
    }

    // Simulates repository operation
    fun saveRepo(country: Country): Long {
        return if (country.code.isNotBlank()) 1
        else throw SQLIntegrityConstraintViolationException("Empty code for: $country")
    }

    fun save(country: Country): Either<String, Long> {
        return Either
            .catch { saveRepo(country) }
            .mapLeft { it.message ?: "Unkown error" }
    }
}

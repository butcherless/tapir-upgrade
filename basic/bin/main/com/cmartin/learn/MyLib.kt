package com.cmartin.learn

import arrow.core.*
import arrow.typeclasses.Semigroup
import com.cmartin.learn.Model.Country
import java.sql.SQLIntegrityConstraintViolationException

object MyLib {

  fun sayHello() = println("Kotlin Hello Woprld")

  fun validateNonEmptyText(text: String): Either<String, String> {
    return if (text.isNullOrBlank()) Either.Left("empty text") else Either.Right(text)
  }

  fun validate2EmptyText(text: String): Validated<String, String> {
    return Validated.fromEither(validateNonEmptyText(text))
  }

  fun validateTextLength(text: String): Validated<String, String> {
    return if (text.length > 8) "Invalid length".invalid() else text.valid()
  }

  fun validateComposed(text: String): Validated<String, String> {
    val parallelValidate =
        1.validNel().zip(Semigroup.nonEmptyList<String>(), 2.validNel()) { a, b -> 0 }

    return validate2EmptyText(text).zip(Semigroup.string(), validateTextLength(text)) { a, b ->
      text
    }
  }

  fun validationOne(text: String): ValidatedNel<Int, String> {
    return 1.invalidNel()
  }

  fun validationTwo(text: String): ValidatedNel<Int, String> {
    return 2.invalidNel()
  }

  fun validationNel(text: String): ValidatedNel<Int, String> {
    return validationOne(text).zip(Semigroup.nonEmptyList(), validationTwo(text)) { a, b -> text }
  }

  // Simulates repository operation
  fun saveRepo(country: Country): Long {
    return if (country.code.isNotBlank()) 1
    else throw SQLIntegrityConstraintViolationException("Empty code for: $country")
  }

  fun save(country: Country): Either<String, Long> {
    return Either.catch { saveRepo(country) }.mapLeft { it.message ?: "Unkown error" }
  }
}

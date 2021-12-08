package com.cmartin.learn

import arrow.core.Either
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLTimeoutException

object ExceptionManagementModule {
    const val INVALID_DATA_MSG = "Invalid data"

    /** Error Model - ADT
     */
    sealed class RepositoryError {
        data class ConnectionError(val value: String) : RepositoryError()
        data class DataIntegrityError(val value: String) : RepositoryError()
        data class SqlSyntaxError(val value: String) : RepositoryError()
        data class UnknownError(val value: String) : RepositoryError()
    }

    /** Simulates repository operations with exception throwing.
     */
    object DummyRepository {
        fun saveRepo(country: Model.Country): Long {
            return if (country.code.isNotBlank()) 1
            else throw SQLIntegrityConstraintViolationException("$INVALID_DATA_MSG: $country")
        }
    }

    /** Service with exception management. Use cases:
     *  a) succeed: Either<_, A> - succeed output type
     *  b) failed:  Either<E, _> - failed  output type
     */
    object DummyService {
        fun save(country: Model.Country): Either<RepositoryError, Long> =
            Either
                .catch { DummyRepository.saveRepo(country) }
                .mapLeft { manageException(it) }
    }

    private fun manageException(th: Throwable): RepositoryError =
        when (th) {
            is SQLIntegrityConstraintViolationException ->
                RepositoryError.DataIntegrityError(th.localizedMessage)
            is SQLTimeoutException ->
                RepositoryError.ConnectionError(th.localizedMessage)
            else ->
                RepositoryError.UnknownError(th.localizedMessage)
        }
}
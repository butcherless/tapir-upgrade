package com.cmartin.learn

import arrow.core.Either
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLTimeoutException

object ExceptionManagementModule {
    const val CONNECTION_ERROR = "There was a problem with the connection"
    const val DATA_INTEGRITY_ERROR = "There was a problem with the data integrity"
    const val SQL_SYNTAX_ERROR = "There was a problem with the SQL syntax"
    const val SQL_UNKNOWN_ERROR = "There was an unspecified problem"
    const val INVALID_DATA_MSG = "Invalid data"

    sealed class RepositoryError(val message: String) {
        data class ConnectionError(val value: String) : RepositoryError(CONNECTION_ERROR)
        data class DataIntegrityError(val value: String) : RepositoryError(DATA_INTEGRITY_ERROR)
        data class SqlSyntaxError(val value: String) : RepositoryError(SQL_SYNTAX_ERROR)
        data class UnknownError(val value: String) : RepositoryError(SQL_UNKNOWN_ERROR)
    }

    /* Simulates repository operations with exception throwing.
     */
    object DummyRepository {
        fun saveRepo(country: Model.Country): Long {
            return if (country.code.isNotBlank()) 1
            else throw SQLIntegrityConstraintViolationException("$INVALID_DATA_MSG: $country")
        }
    }

    object DummyService {
        fun save(country: Model.Country): Either<RepositoryError, Long> {
            return Either
                .catch { DummyRepository.saveRepo(country) }
                .mapLeft { manageException(it) }
        }
    }

    private fun manageException(th: Throwable): RepositoryError {
        return when (th) {
            is SQLIntegrityConstraintViolationException ->
                RepositoryError.DataIntegrityError(th.localizedMessage)
            is SQLTimeoutException ->
                RepositoryError.ConnectionError(th.localizedMessage)
            else ->
                RepositoryError.UnknownError(th.localizedMessage)
        }
    }
}
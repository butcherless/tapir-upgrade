package com.cmartin.learn

import arrow.core.*
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.cmartin.learn.CmdbApiClientImpl.Companion.CmdbApiClientError.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

@Component
class CmdbApiClientImpl(private val deps: CmdbClientDeps) {
    private val baseOusUrl = "${deps.baseUrl}/api/v1/uos"
    protected val logger = LoggerFactory.getLogger(javaClass)!!

    fun getDescendantsOus(code: String): Either<CmdbApiClientError, List<DescendantView>> =
        try {
            getAuthToken().flatMap { token ->
                getParentOu(code, token).flatMap { ouCode ->
                    getDescendants(ouCode, token)
                }
            }
        } catch (th: Throwable) {
            manageErrors(th)
        }


    fun getAuthToken(): Either<CmdbApiClientError, String> =
        URI.create("${deps.baseUrl}/oauth2/token").right()
            .tap { uri -> logger.debug("getAuthToken - URI: $uri") }
            .flatMap { uri ->
                deps.webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers { it.setBasicAuth(deps.authToken) }
                    .body(BodyInserters.fromFormData(buildAuthData()))
                    .exchangeToMono { extractResponse<Map<String, String>>(it) }.block()
                    .resToEither(EmptyResponse(EMPTY_TOKEN_RESPONSE))
            }.flatMap { pair ->
                manageHttpCode(pair.first)
                    .flatMap {
                        retrieveToken(pair.second)
                    }
            }

    fun getParentOu(code: String, token: String): Either<CmdbApiClientError, String> =
        URI.create("$baseOusUrl/search").right()
            .tap { uri -> logger.debug("getParentOu - [$code,$uri]") }
            .flatMap { uri ->
                deps.webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers { buildAuthHeaders(token, it) }
                    .body(buildRootData(code), Map::class.java)
                    //.exchangeToMono { r -> r.toMono() }
                    .exchange()
                    .block().resToEither(EmptyResponse(EMPTY_PARENT_OU_RESPONSE))
            }.flatMap { response ->
                manageHttpCode(response.statusCode()).flatMap {
                    retrieveParentId(response, code)
                }
            }

    fun getDescendants(code: String, token: String): Either<CmdbApiClientError, List<DescendantView>> =
        URI.create("$baseOusUrl/busquedasDescendientes").right()
            .tap { uri -> logger.debug("getDescendants - [$code,$uri]") }
            .flatMap { uri ->
                deps.webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers { buildAuthHeaders(token, it) }
                    .body(buildDescendantsData(code), Map::class.java)
                    //.exchangeToMono { r -> r.toMono() }
                    .exchange()
                    .block().resToEither(EmptyResponse(EMPTY_DESCENDANT_OU_RESPONSE))
            }.flatMap { response ->
                manageHttpCode(response.statusCode()).flatMap {
                    retrieveDescendants(response, code)
                }
            }

    /* Map Exceptions to DomainError as required
        when (th) {
            ... ->
            ... ->
            else -> DefaultError(th.localizedMessage)
        }.left()
     */
    private fun manageErrors(th: Throwable): Either<CmdbApiClientError, Nothing> =
        DefaultError(th.localizedMessage).left()


    private fun manageHttpCode(code: HttpStatus): Either<CmdbApiClientError, HttpStatus> {
        val message = "status code: $code"
        return when (code.series()) {
            HttpStatus.Series.SUCCESSFUL -> code.right()
            HttpStatus.Series.CLIENT_ERROR -> InvalidRequest(message).left()
            else -> ServerError(message).left()
        }
    }

    private fun retrieveToken(map: Map<String, String>): Either<CmdbApiClientError, String> =
        map[ACCESS_TOKEN_KEY]?.let { Right(it) } ?: MissingToken("missing token key: $ACCESS_TOKEN_KEY").left()

    private fun retrieveParentId(response: ClientResponse, code: String): Either<CmdbApiClientError, String> =
        response.bodyToEither<DescendantsView>(EmptyResponse("Empty body for parent OU request"))
            .tap { view -> logger.debug("retrieveParentId - number of OUs retrieved: ${view.numeroTotalRegistros}") }
            .flatMap { view ->
                if (view.numeroTotalRegistros == 0) NoResults("$NO_RESULTS_OU_CODE: $code").left()
                else view.listaResultado.first().id.toString().right()
            }

    private fun retrieveDescendants(
        response: ClientResponse,
        code: String
    ): Either<CmdbApiClientError, List<DescendantView>> =
        response.bodyToEither<DescendantsView>(EmptyResponse("Empty body for descendants request"))
            .tap { view -> logger.debug("retrieveDescendants - number of OUs retrieved: ${view.numeroTotalRegistros}") }
            .flatMap { view ->
                if (view.numeroTotalRegistros == 0) NoResults("$NO_RESULTS_OU_CODE: $code").left()
                else view.listaResultado.right()
            }


    private fun buildAuthData(): MultiValueMap<String, String> {
        val formData = LinkedMultiValueMap<String, String>()
        formData.add("grant_type", "client_credentials")
        formData.add("scope", "operate-gestionserviciosssii")
        return formData
    }

    private fun buildAuthHeaders(token: String, it: HttpHeaders?) {
        it?.setBearerAuth(token)
        it?.set("scope", "operate-gestionserviciosssii")
        it?.set("X-IBM-Client-Id", deps.clientId)
    }

    private fun buildRootData(code: String): Mono<Map<String, Array<String>>> {
        return Mono.just(
            mapOf("codsTalentia" to arrayOf(code))
        )
    }

    private fun buildDescendantsData(code: String): Mono<Map<String, Any>> {
        return Mono.just(
            mapOf(
                "idPadre" to code,
                "primerNivel" to false,
                "conStaff" to false
            )
        )
    }


    companion object {
        const val MISSING_TOKEN = "no token info"
        const val ACCESS_TOKEN_KEY = "access_token"
        const val EMPTY_TOKEN_RESPONSE = "empty response for token request"
        const val EMPTY_PARENT_OU_RESPONSE = "empty response for parent OU request"
        const val NO_RESULTS_OU_CODE = "empty results for OU code"
        const val EMPTY_DESCENDANT_OU_RESPONSE = "empty response for descendant OUs request"

        // Abstract Error
        interface DescribedError {
            val message: String
        }

        // Client Error ADT
        sealed class CmdbApiClientError : DescribedError {
            data class InvalidRequest(override val message: String) : CmdbApiClientError()
            data class EmptyResponse(override val message: String) : CmdbApiClientError()
            data class MissingToken(override val message: String) : CmdbApiClientError()
            data class ServerError(override val message: String) : CmdbApiClientError()
            data class NoResults(override val message: String) : CmdbApiClientError()
            data class DefaultError(override val message: String) : CmdbApiClientError()
        }

        data class DescendantsView(
            val numeroTotalRegistros: Int,
            val listaResultado: List<DescendantView>
        )

        data class DescendantView(
            val active: Boolean,
            val description: String,
            val id: Int,
            val yearOuId: Int,
            val managerId: Int,
            val typeId: Int,
            val ouId: String,
            val parentOuId: Int
        )

        private inline fun <reified T : Any> extractResponse(res: ClientResponse): Mono<Pair<HttpStatus, T>> =
            res.bodyToMono<T>()
                .map { a -> Pair(res.statusCode(), a) }

        private inline fun <reified T : Any> ClientResponse.option(): Option<T> {
            return this.bodyToMono<T>().block().toOption()
        }

        private inline fun <reified T : Any> T?.resToEither(error: CmdbApiClientError): Either<CmdbApiClientError, T> =
            this?.let { Right(it) } ?: Left(error)

        private fun ClientResponse.toMono(): Mono<ClientResponse> =
            Mono.just(this)

        private fun ClientResponse?.resToEither(error: CmdbApiClientError): Either<CmdbApiClientError, ClientResponse> =
            this?.right() ?: error.left()

        private inline fun <reified T : Any> ClientResponse.bodyToEither(error: CmdbApiClientError): Either<CmdbApiClientError, T> =
            this.bodyToMono<T>().block()?.let { Right(it) } ?: Left(error)

    }
}
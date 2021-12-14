package com.cmartin.learn

import com.cmartin.learn.CmdbApiClientImpl.Companion.ACCESS_TOKEN_KEY
import com.cmartin.learn.CmdbApiClientImpl.Companion.CmdbApiClientError.InvalidRequest
import com.cmartin.learn.CmdbApiClientImpl.Companion.CmdbApiClientError.MissingToken
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.web.reactive.function.client.WebClient

@SpringJUnitConfig
class CmdbApiClientTest {
    private val rootOuCode = "1001"
    private val authToken = """{ "access_token": "dummy-token" }"""

    val webClient: WebClient = WebClient.create()

    @Test
    fun shouldCreateDependencies() {
        Assertions.assertNotNull(webClient, "web client not available")
    }

    @Test
    fun shouldFailToRetrieveAuthToken() {
        // given
        val expectedFailure = MissingToken("missing token key: $ACCESS_TOKEN_KEY")
        val port = 10001
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = MockWebServer()
        setBodyJson(server, "{}")
        server.start(port)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        // then
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertEquals(expectedFailure, it)
        }
        server.shutdown()
    }

    @Test
    fun shouldFailToForUnauthorizedRequest() {
        // given
        val expectedFailure = InvalidRequest("status code: ${HttpStatus.UNAUTHORIZED}")
        val port = 10002
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = MockWebServer()
        setBodyJson(server, authToken)
        setResponseCodeJson(server, HttpStatus.UNAUTHORIZED)
        server.start(port)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        // then
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertEquals(expectedFailure, it)
        }
        server.shutdown()
    }

    @Test
    fun shouldFailToForNotFoundRequest() {
        // given
        val expectedFailure = InvalidRequest("status code: ${HttpStatus.NOT_FOUND}")
        val port = 10003
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = MockWebServer()
        setBodyJson(server, authToken)
        setResponseCodeJson(server, HttpStatus.NOT_FOUND)
        server.start(port)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertEquals(expectedFailure, it)
        }
        server.shutdown()
    }

    @Test
    fun shouldFailToForConnectionRefusedErrorUNIT() {
        // given
        val expectedFailureMessage = "Connection refused"
        val port = 10004
        val deps = CmdbClientDeps(buildUrl(11111), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = MockWebServer()
        setBodyJson(server, authToken)
        setResponseCodeJson(server, HttpStatus.NOT_FOUND)
        server.start(port)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertTrue(it.message.contains(expectedFailureMessage))
        }
        server.shutdown()
    }

        fun buildUrl(port: Int): String {
        return "http://localhost:$port"
    }

    fun setBodyJson(server: MockWebServer, body: String) {
        server.enqueue(
            MockResponse().setBody(body)
                .addHeader("Content-Type", "application/json")
        )
    }

    fun setResponseCodeJson(server: MockWebServer, status: HttpStatus) {
        server.enqueue(
            MockResponse().setResponseCode(status.value())
                .addHeader("Content-Type", "application/json")
        )
    }


}
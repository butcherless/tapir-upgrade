package com.cmartin.learn

import com.cmartin.learn.CmdbApiClientImpl.Companion.ACCESS_TOKEN_KEY
import com.cmartin.learn.CmdbApiClientImpl.Companion.CmdbApiClientError.*
import com.cmartin.learn.CmdbApiClientImpl.Companion.NO_RESULTS_OU_CODE
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
    fun shouldRetrieveDescendantOus() {
        // given
        val port = 9000
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = startServerAndPost(port, authToken)
        setBodyJson(server, parentOuResponse)
        setBodyJson(server, descendantOusResponse)
        // when
        val result = client.getDescendantsOus(rootOuCode)
        // then
        Assertions.assertTrue(result.isRight())
        result.map {
            Assertions.assertEquals(1, it.size)
        }
        server.shutdown()
    }


    @Test
    fun shouldFailToRetrieveAuthToken() {
        // given
        val expectedFailure = MissingToken("missing token key: $ACCESS_TOKEN_KEY")
        val port = 10000
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = startServerAndPost(port, "{}")
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
        val port = 10001
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = startServerAndPost(port, authToken)
        setResponseCodeJson(server, HttpStatus.UNAUTHORIZED)
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
        val server = startServerAndPost(port, authToken)
        setResponseCodeJson(server, HttpStatus.NOT_FOUND)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertEquals(expectedFailure, it)
        }
        server.shutdown()
    }

    @Test
    fun shouldFailToForConnectionRefusedError() {
        // given
        val expectedFailureMessage = "Connection refused"
        val port = 10004
        val deps = CmdbClientDeps(buildUrl(10999), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server
        val server = startServerAndPost(port, authToken)
        setResponseCodeJson(server, HttpStatus.NOT_FOUND)
        // when
        val failure = client.getDescendantsOus(rootOuCode)
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertTrue(it.message.contains(expectedFailureMessage))
        }
        server.shutdown()
    }

    @Test
    fun shouldFailToRetrieveDescendantsForMissingOuCode() {
        // given
        val code = "0"
        val expectedFailure = NoResults("$NO_RESULTS_OU_CODE: $code")
        val port = 10005
        val deps = CmdbClientDeps(buildUrl(port), "dummy-token", "dummy-id", webClient)
        val client = CmdbApiClientImpl(deps)
        // server behavior
        val server = startServerAndPost(port, authToken)
        setBodyJson(server, emptyResultsResponse)
        // when
        val failure = client.getDescendantsOus(code)
        // then
        Assertions.assertTrue(failure.isLeft())
        failure.mapLeft {
            Assertions.assertEquals(expectedFailure, it)
        }
        server.shutdown()
    }

    fun buildUrl(port: Int): String {
        return "http://localhost:$port"
    }

    fun setBodyJson(server: MockWebServer, body: String) {
        server.enqueue(
            MockResponse().setResponseCode(HttpStatus.CREATED.value())
                .setBody(body)
                .addHeader("Content-Type", "application/json")
        )
    }

    fun setResponseCodeJson(server: MockWebServer, status: HttpStatus) {
        server.enqueue(
            MockResponse().setResponseCode(status.value())
                .addHeader("Content-Type", "application/json")
        )
    }

    fun startServerAndPost(port: Int, body: String): MockWebServer {
        val server = MockWebServer()
        setBodyJson(server, body)
        server.start(port)
        return server
    }

    val emptyResultsResponse = """
        {
          "numeroTotalRegistros": 0,
          "listaResultado": []
        }
    """.trimIndent()

    val descendantOusResponse = """
        {
          "numeroTotalRegistros": 1,
          "listaResultado": [
            {
              "id": 7332,
              "ouId": "600000790",
              "description": "TECNOLOGIA",
              "idTipo": 3,
              "activo": true,
              "idAnioUO": 125,
              "idUoPadre": 7444,
              "idResponsable": 416
            }
          ]
        }
    """.trimIndent()

    val parentOuResponse = """
        {
            "numeroTotalRegistros": 1,
            "listaResultado": [
                {
                    "active": true,
                    "description": "SISTEMAS DE INFORMACION",
                    "id": 7444,
                    "yearOuId": 125,
                    "managerId": 7511,
                    "typeId": 2,
                    "ouId": "600000745",
                    "parentOuId": 16833
                }
            ]
        }
    """.trimIndent()

}
package com.cmartin.learn

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
data class CmdbClientDeps(
    @Value("\${cmdb.url}") val baseUrl: String,
    @Value("\${cmdb.authToken}") val authToken: String,
    @Value("\${cmdb.clientId}") val clientId: String,
    val webClient: WebClient
)
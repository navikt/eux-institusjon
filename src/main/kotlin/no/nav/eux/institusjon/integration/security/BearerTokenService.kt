package no.nav.eux.institusjon.integration.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.institusjon.integration.security.BearerTokenService.Client.*
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDateTime.now

@Service
class BearerTokenService(
    val clientProperties: ClientProperties,
    val clientTokens: MutableMap<Client, BearerToken>
) {

    val log = logger {}

    @Retryable(maxAttempts = 4, backoff = Backoff(delay = 1000, multiplier = 2.0))
    fun fetch(client: Client): BearerToken =
        try {
            val bearerToken = clientTokens[client]
            if (bearerToken.upForRenewal() || bearerToken == null) {
                val token = client.token()
                clientTokens[client] = token
                token
            } else {
                bearerToken
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to fetch token" }
            throw e
        }

    fun BearerToken?.upForRenewal() =
        if (this != null)
            now().plusSeconds(10).isAfter(expiry)
        else
            true

    fun Client.token(): BearerToken {
        val token = RestClient.create()
            .post()
            .uri(clientProperties.tokenEndpoint)
            .contentType(APPLICATION_FORM_URLENCODED)
            .accept(APPLICATION_FORM_URLENCODED)
            .body(createBody())
            .retrieve()
            .body<TokenResponse>()!!
            .bearerToken
        log.info { "Successfully retrieved token for $this" }
        return token
    }

    fun Client.createBody() =
        LinkedMultiValueMap<String, String>()
            .apply {
                add("grant_type", "client_credentials")
                add("client_id", clientProperties.id)
                add("client_secret", clientProperties.secret)
                add("scope", scope)
            }

    val Client.scope
        get() =
            when (this) {
                EUX_RINA_API -> clientProperties.euxRinaApi.scope
            }

    enum class Client {
        EUX_RINA_API,
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TokenResponse(
        @JsonProperty("token_type")
        val tokenType: String,
        @JsonProperty("expires_in")
        val expiresIn: Long,
        @JsonProperty("access_token")
        val accessToken: String
    ) {
        val bearerToken
            get() = BearerToken(accessToken, now().plusSeconds(expiresIn))
    }

}

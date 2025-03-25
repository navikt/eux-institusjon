package no.nav.eux.institusjon.integration.config

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.institusjon.integration.security.BearerToken
import no.nav.eux.institusjon.integration.security.BearerTokenService
import no.nav.eux.institusjon.integration.security.BearerTokenService.Client.EUX_RINA_API
import no.nav.eux.institusjon.integration.security.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.EnableRetry
import org.springframework.web.client.RestClient

@EnableRetry
@Configuration
class IntegrationConfig {

    val log = logger {}

    @Bean
    fun clientTokens() = HashMap<BearerTokenService.Client, BearerToken>()

    @Bean
    fun euxRinaApiRestClient(
        clientProperties: ClientProperties,
        bearerTokenService: BearerTokenService
    ) = RestClient
        .builder()
        .baseUrl(clientProperties.euxRinaApi.url)
        .requestInterceptor(bearerTokenService interceptorFor EUX_RINA_API)
        .build()

    @Bean
    fun retryListener() = object : RetryListener {
        override fun <T, E : Throwable?> onError(
            context: RetryContext,
            callback: RetryCallback<T, E>,
            throwable: Throwable
        ) {
            log.warn(throwable) {
                "Eksternt kall feilet: ${context.getAttribute("context.name")}, forsøk nr: ${context.retryCount}"
            }
        }
    }

    infix fun BearerTokenService.interceptorFor(
        client: BearerTokenService.Client
    ) = ClientHttpRequestInterceptor { request, body, execution ->
        val token = fetch(client).token
        request.headers.setBearerAuth(token)
        execution.execute(request, body)
    }
}
package no.nav.eux.institusjon.integration.client

import no.nav.eux.institusjon.integration.client.model.EuxInstitusjon
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class EuxRinaApiClient(
    val euxRinaApiRestClient: RestClient
) {

    @Retryable(maxAttempts = 9, backoff = Backoff(delay = 1000, multiplier = 2.0))
    fun getInstitusjoner(bucType: String, landkode: String) = euxRinaApiRestClient
        .get()
        .uri("/cpi/institusjoner?BuCType=$bucType&LandKode=$landkode&domene=nav")
        .accept(APPLICATION_JSON)
        .retrieve()
        .body<List<EuxInstitusjon>>()
        ?: throw RuntimeException("Institusjoner ikke funnet")

}

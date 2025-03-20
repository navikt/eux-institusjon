package no.nav.eux.institusjon.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.institusjon.integration.client.EuxRinaApiClient
import no.nav.eux.institusjon.integration.client.model.EuxInstitusjon
import no.nav.eux.institusjon.model.Institusjon
import org.springframework.stereotype.Service

@Service
class InstitusjonService(
    val euxRinaApiClient: EuxRinaApiClient,
    val institutionCache: InstitutionCache,
) {

    val log = logger {}

    fun institusjoner(bucType: String, landkode: String): List<Institusjon> {
        val key = InstitutionCache.Key(bucType, landkode)
        val institusjon = institutionCache[key]
        if (institusjon != null) {
            log.info { "henter inst fra cache for $bucType, landkode: $landkode" }
            return institusjon
        } else {
            val institusjoner = euxRinaApiClient
                .getInstitusjoner(bucType, landkode)
                .institusjoner(bucType)
            institutionCache[key] = institusjoner
            log.info { "Oppdaterte cache for $bucType, landkode: $landkode" }
            return institusjoner
        }
    }
}

fun List<EuxInstitusjon>.institusjoner(bucType: String): List<Institusjon> {
    return this
        .filter { euxInstitusjon ->
            euxInstitusjon.tilegnetBucs.any {
                it.bucType.uppercase() == bucType.uppercase() &&
                        it.institusjonsrolle == "CounterParty"
            }
        }
        .map {
            Institusjon(
                institusjonId = it.id,
                navn = "${it.id} - ${it.navn}",
                landkode = it.landkode,
                bucType = bucType
            )
        }
}
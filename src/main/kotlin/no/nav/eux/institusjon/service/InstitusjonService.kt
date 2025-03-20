package no.nav.eux.institusjon.service

import no.nav.eux.institusjon.integration.client.EuxRinaApiClient
import no.nav.eux.institusjon.integration.client.model.EuxInstitusjon
import no.nav.eux.institusjon.model.Institusjon
import org.springframework.stereotype.Service

@Service
class InstitusjonService(
    val euxRinaApiClient: EuxRinaApiClient
) {

    fun institusjoner(bucType: String, landkode: String): List<Institusjon> {
        val euxInstitusjoner = euxRinaApiClient.getInstitusjoner(bucType, landkode)
        return euxInstitusjoner.institusjoner(bucType)
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
}

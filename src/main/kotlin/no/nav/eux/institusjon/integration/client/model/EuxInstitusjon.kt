package no.nav.eux.institusjon.integration.client.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class EuxInstitusjon(
    val id: String,
    val navn: String,
    val akronym: String,
    val landkode: String,
    @JsonProperty("tilegnetBucs")
    val tilegnetBucs: List<TilegnetBuc>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TilegnetBuc(
    val bucType: String,
    val institusjonsrolle: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val gyldigStartDato: ZonedDateTime,
    val eessiklar: Boolean
)

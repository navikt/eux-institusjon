package no.nav.eux.institusjon.webapp.api

import io.swagger.v3.oas.annotations.Parameter
import no.nav.eux.institusjon.model.Institusjon
import no.nav.eux.institusjon.service.InstitusjonService
import no.nav.eux.institusjon.webapp.model.InstitusjonApiModel
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class InstitusjonController(
    val institusjonService: InstitusjonService
) {

    @Protected
    @GetMapping(
        value = ["/api/v1/institusjoner"],
        produces = ["application/json"]
    )
    fun getInstitusjon(
        @Parameter(example = "FB_BUC_01", required = true)
        @RequestParam(value = "bucType")
        bucType: String,
        @Parameter(example = "NOR")
        @RequestParam(value = "landkode", required = false, defaultValue = "")
        landkode: String,
    ): ResponseEntity<List<InstitusjonApiModel>> {
        return ResponseEntity<List<InstitusjonApiModel>>(
            institusjonService
                .institusjoner(bucType, landkode)
                .toInstitusjonApiModel(),
            OK
        )
    }

    fun List<Institusjon>.toInstitusjonApiModel() = this.map {
        InstitusjonApiModel(
            institusjonId = it.institusjonId,
            navn = it.navn,
            landkode = it.landkode,
            bucType = it.bucType,
        )
    }
}

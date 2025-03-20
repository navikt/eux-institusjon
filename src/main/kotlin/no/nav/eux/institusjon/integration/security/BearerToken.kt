package no.nav.eux.institusjon.integration.security

import java.time.LocalDateTime

data class BearerToken(
    val token: String,
    val expiry: LocalDateTime
)

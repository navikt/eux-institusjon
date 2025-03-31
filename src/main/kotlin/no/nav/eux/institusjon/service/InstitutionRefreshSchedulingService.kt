package no.nav.eux.institusjon.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.institusjon.integration.client.EuxRinaApiClient
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class InstitutionRefreshSchedulingService(
    val euxRinaApiClient: EuxRinaApiClient,
    val institutionCache: InstitutionCache,
) {

    val log = logger {}

    @EventListener(ApplicationReadyEvent::class)
    fun refreshOnStartup() {
        log.info { "Triggering asynchronous institution list refresh on startup" }
        CompletableFuture.runAsync {
            try {
                log.info { "Starting asynchronous institution list refresh" }
                refreshInstitutionList()
                log.info { "Completed asynchronous institution list refresh" }
            } catch (e: Exception) {
                log.error(e) { "Failed to refresh institution list on startup" }
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    fun scheduledRefresh() {
        log.info { "Scheduled refresh at 4 AM" }
        refreshInstitutionList()
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun clearList() {
        institutionCache.clear()
    }

    fun refreshInstitutionList() {
        log.info { "Refreshing institution list" }
        landkoder.forEach { refreshInstitutionList(it) }
    }

    fun refreshInstitutionList(landkode: String) {
        bucTyper.forEach { refreshInstitutionList(landkode, it) }
    }

    fun refreshInstitutionList(landkode: String, bucType: String) {
        try {
            val institusjoner = euxRinaApiClient
                .getInstitusjoner(bucType, landkode)
                .institusjoner(bucType)
            val key = InstitutionCache.Key(bucType, landkode)
            institutionCache[key] = institusjoner
            log.info { "Oppdaterte cache for buc type $bucType, landkode: $landkode" }
        } catch (e: Exception) {
            log.error(e) { "Failed to refresh institution list for $bucType, landkode: $landkode" }
        }
    }
}

val landkoder = listOf(
    "NOR",
    "DNK",
    "SWE",
    "BEL",
    "POL",
    "BGR",
)

val bucTyper = listOf(
    "H_BUC_01",
    "H_BUC_02a",
    "H_BUC_02b",
    "H_BUC_02c",
    "H_BUC_03a",

    "FB_BUC_01",
    "FB_BUC_02",
    "FB_BUC_03",
    "FB_BUC_04",

    "S_BUC_12",
    "S_BUC_14",
    "S_BUC_14a",
    "S_BUC_14b",
    "S_BUC_15",
    "S_BUC_17",
    "S_BUC_24",

    "UB_BUC_01",
    "UB_BUC_02",
    "UB_BUC_03",
    "UB_BUC_04",
)
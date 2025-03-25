package no.nav.eux.institusjon.service

import no.nav.eux.institusjon.model.Institusjon
import org.springframework.stereotype.Component

@Component
class InstitutionCache(
    val institusjonerMap: HashMap<Key, List<Institusjon>>,
) {

    operator fun get(key: Key): List<Institusjon>? =
        institusjonerMap[key]

    operator fun set(key: Key, institusjoner: List<Institusjon>) {
        institusjonerMap[key] = institusjoner
    }

    fun clear() {
        institusjonerMap.clear()
    }

    data class Key(
        val bucType: String,
        val landkode: String,
    )
}



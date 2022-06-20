package no.nav.fo.veilarbregistrering.arbeidssoker

import no.nav.common.featuretoggle.UnleashClient
import no.nav.fo.veilarbregistrering.bruker.Bruker
import no.nav.fo.veilarbregistrering.bruker.Periode
import no.nav.fo.veilarbregistrering.log.logger
import no.nav.fo.veilarbregistrering.metrics.Events
import no.nav.fo.veilarbregistrering.metrics.JaNei
import no.nav.fo.veilarbregistrering.metrics.Metric
import no.nav.fo.veilarbregistrering.metrics.MetricsService
import org.springframework.stereotype.Service

@Service
class ArbeidssokerService(
    private val formidlingsgruppeRepository: FormidlingsgruppeRepository,
    private val formidlingsgruppeGateway: FormidlingsgruppeGateway,
    private val unleashClient: UnleashClient,
    private val metricsService: MetricsService
) {

    fun hentArbeidssokerperioder(bruker: Bruker, forespurtPeriode: Periode?): Arbeidssokerperioder {
        val arbeidssokerperioderLokalt =
            formidlingsgruppeRepository.finnFormidlingsgrupper(bruker.alleFoedselsnummer())
        val arbeidssokerperioderORDS =
            formidlingsgruppeGateway.finnArbeissokerperioder(bruker.gjeldendeFoedselsnummer, forespurtPeriode!!)

        val dekkerHele = arbeidssokerperioderLokalt.dekkerHele(forespurtPeriode)
        val overlappendeArbeidssokerperioderLokalt = arbeidssokerperioderLokalt.overlapperMed(forespurtPeriode)
        val overlappendeHistoriskePerioderORDS = arbeidssokerperioderORDS.overlapperMed(forespurtPeriode)
        val lokalErLikOrds = overlappendeArbeidssokerperioderLokalt.equals(overlappendeHistoriskePerioderORDS)

        metricsService.registrer(
            Events.HENT_ARBEIDSSOKERPERIODER_KILDER_GIR_SAMME_SVAR,
            if (lokalErLikOrds) JaNei.JA else JaNei.NEI
        )

        if (!lokalErLikOrds) {
            logger.warn(
                "Periodelister fra lokal cache og Arena-ORDS er ikke like\n" +
                        "Forespurt periode: $forespurtPeriode\n" +
                        "Lokalt: $overlappendeArbeidssokerperioderLokalt\n" +
                        "Arena-ORDS: $overlappendeHistoriskePerioderORDS")
        }

        if (dekkerHele && brukLokalCache()) {
            metricsService.registrer(Events.HENT_ARBEIDSSOKERPERIODER_KILDE, Kilde.LOKAL)
            logger.info("Arbeidssokerperiodene fra egen database dekker hele perioden, og returneres: "
                    + "$overlappendeArbeidssokerperioderLokalt")
            return overlappendeArbeidssokerperioderLokalt
        }

        metricsService.registrer(Events.HENT_ARBEIDSSOKERPERIODER_KILDE, Kilde.ORDS)
        logger.info(
                "Returnerer arbeidssokerperioder fra Arena sin ORDS-tjenesten: $overlappendeHistoriskePerioderORDS")
        return overlappendeHistoriskePerioderORDS
    }

    private fun brukLokalCache(): Boolean {
        return unleashClient.isEnabled(VEILARBREGISTRERING_FORMIDLINGSGRUPPE_LOCALCACHE)
    }

    private enum class Kilde : Metric {
        ORDS, LOKAL;

        override fun fieldName(): String {
            return "kilde"
        }

        override fun value(): Any {
            return this.toString()
        }
    }

    companion object {
        const val VEILARBREGISTRERING_FORMIDLINGSGRUPPE_LOCALCACHE = "veilarbregistrering.formidlingsgruppe.localcache"
    }
}
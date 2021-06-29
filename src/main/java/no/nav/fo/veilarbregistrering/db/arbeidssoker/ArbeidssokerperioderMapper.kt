package no.nav.fo.veilarbregistrering.db.arbeidssoker

import no.nav.fo.veilarbregistrering.arbeidssoker.Arbeidssokerperiode
import no.nav.fo.veilarbregistrering.arbeidssoker.Arbeidssokerperiode.EldsteFoerst
import no.nav.fo.veilarbregistrering.arbeidssoker.Arbeidssokerperioder
import no.nav.fo.veilarbregistrering.arbeidssoker.Formidlingsgruppe
import no.nav.fo.veilarbregistrering.bruker.Periode
import no.nav.fo.veilarbregistrering.db.arbeidssoker.Formidlingsgruppeendring.NyesteFoerst
import java.time.LocalDate
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

internal object ArbeidssokerperioderMapper {
    fun map(formidlingsgruppeendringer: List<Formidlingsgruppeendring>): Arbeidssokerperioder {
        return Arbeidssokerperioder(
            Optional.of(
                formidlingsgruppeendringer.stream()
                    .sorted(NyesteFoerst.nyesteFoerst())
                    .collect(Collectors.toList())
            )
                .map(beholdKunEndringerForAktiveIdenter)
                .map(::slettTekniskeISERVEndringer)
                .map(::beholdKunSisteEndringPerDagIListen)
                .map(::populerTilDatoMedNestePeriodesFraDatoMinusEn)
                .get()
                .stream()
                .sorted(EldsteFoerst.eldsteFoerst())
                .collect(Collectors.toList())
        )
    }

    private val beholdKunEndringerForAktiveIdenter =
        Function { formidlingsgruppeendringer: List<Formidlingsgruppeendring> ->
            formidlingsgruppeendringer.stream()
                .filter { obj: Formidlingsgruppeendring -> obj.erAktiv() }
                .collect(Collectors.toList())
        }

    private fun slettTekniskeISERVEndringer(formidlingsgruppeendringer: List<Formidlingsgruppeendring>) =
        formidlingsgruppeendringer.groupBy { it.formidlingsgruppeEndret }
            .values.flatMap { samtidigeEndringer -> if (samtidigeEndringer.size > 1) samtidigeEndringer.filter { !it.erISERV() } else samtidigeEndringer }
            .sortedWith(NyesteFoerst.nyesteFoerst())

    private fun beholdKunSisteEndringPerDagIListen(formidlingsgruppeendringer: List<Formidlingsgruppeendring>): List<Arbeidssokerperiode> {
        val arbeidssokerperioder: MutableList<Arbeidssokerperiode> = mutableListOf()

        var forrigeEndretDato = LocalDate.MAX
        for (formidlingsgruppeendring in formidlingsgruppeendringer) {
            val endretDato = formidlingsgruppeendring.formidlingsgruppeEndret.toLocalDateTime().toLocalDate()
            if (endretDato.isEqual(forrigeEndretDato)) {
                continue
            }
            arbeidssokerperioder.add(
                Arbeidssokerperiode(
                    Formidlingsgruppe.of(formidlingsgruppeendring.formidlingsgruppe),
                    Periode.of(
                        endretDato,
                        null
                    )
                )
            )
            forrigeEndretDato = endretDato
        }

        return arbeidssokerperioder
    }

    private fun populerTilDatoMedNestePeriodesFraDatoMinusEn(arbeidssokerperioder: List<Arbeidssokerperiode>): List<Arbeidssokerperiode> =
        arbeidssokerperioder.mapIndexed { index, arbeidssokerperiode ->
            val forrigePeriodesFraDato = if (index > 0) arbeidssokerperioder[index - 1].periode?.fra else null
            arbeidssokerperiode.tilOgMed(forrigePeriodesFraDato?.minusDays(1))
        }
}
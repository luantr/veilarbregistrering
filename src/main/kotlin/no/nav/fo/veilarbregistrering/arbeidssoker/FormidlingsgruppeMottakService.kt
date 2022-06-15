package no.nav.fo.veilarbregistrering.arbeidssoker

import no.nav.fo.veilarbregistrering.log.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FormidlingsgruppeMottakService(
    private val arbeidssokerRepository: ArbeidssokerRepository,
    private val arbeidssokerperiodeAvsluttetService: ArbeidssokerperiodeAvsluttetService
) {

    @Transactional
    fun behandle(endretFormidlingsgruppeCommand: EndretFormidlingsgruppeCommand) {

        val foedselsnummer = endretFormidlingsgruppeCommand.foedselsnummer

        if (foedselsnummer == null) {
            logger.warn(
                String.format(
                    "Foedselsnummer mangler for EndretFormidlingsgruppeCommand med person_id = %s",
                    endretFormidlingsgruppeCommand.personId
                )
            )
            return
        }
        if (endretFormidlingsgruppeCommand.formidlingsgruppeEndret.isBefore(LocalDateTime.parse("2010-01-01T00:00:00"))) {
            logger.warn(
                String.format(
                    "Foreldet formidlingsgruppe-endring (%s) lest fra topic: 'gg-arena-formidlinggruppe-v1'  - denne forkastes.",
                    endretFormidlingsgruppeCommand.formidlingsgruppeEndret
                )
            )
            return
        }

        val eksisterendeArbeidssokerperioderLokalt = arbeidssokerRepository.finnFormidlingsgrupper(
            listOf(foedselsnummer)
        )

        arbeidssokerRepository.lagre(endretFormidlingsgruppeCommand)

        arbeidssokerperiodeAvsluttetService.behandleAvslutningAvArbeidssokerperiode(
            endretFormidlingsgruppeCommand,
            eksisterendeArbeidssokerperioderLokalt
        )

    }
}
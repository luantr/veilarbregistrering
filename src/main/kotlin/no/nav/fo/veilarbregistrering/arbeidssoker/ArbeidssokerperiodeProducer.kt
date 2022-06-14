package no.nav.fo.veilarbregistrering.arbeidssoker

import no.nav.fo.veilarbregistrering.log.logger

class ArbeidssokerperiodeProducer {

    fun publiserArbeidssokerperiodeAvsluttet(endretFormidlingsgruppeCommand: EndretFormidlingsgruppeCommand) {
        logger.info("Ny formidlingsgruppe for person: ${endretFormidlingsgruppeCommand.formidlingsgruppe} - arbeidssøkerperiode avsluttet ${endretFormidlingsgruppeCommand.formidlingsgruppeEndret}")
    }
}
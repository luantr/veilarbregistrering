package no.nav.fo.veilarbregistrering.feil

import no.nav.fo.veilarbregistrering.bruker.feil.*
import no.nav.fo.veilarbregistrering.log.loggerFor
import no.nav.fo.veilarbregistrering.oppfolging.HentOppfolgingStatusException
import no.nav.fo.veilarbregistrering.oppgave.OppgaveAlleredeOpprettet
import no.nav.fo.veilarbregistrering.registrering.bruker.AktiverBrukerException
import no.nav.fo.veilarbregistrering.registrering.bruker.KanIkkeReaktiveresException
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class FeilHandtering : ResponseEntityExceptionHandler() {

    @ExceptionHandler(AktiverBrukerException::class)
    fun aktiverBrukerException(f: AktiverBrukerException): ResponseEntity<FeilDto> =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(FeilDto(f.aktiverBrukerFeil.toString()))

    @ExceptionHandler(KontaktinfoIngenTilgang::class)
    fun kontaktInfoIngenTilgang(feil: KontaktinfoIngenTilgang) =
        ResponseEntity.status(FORBIDDEN)
            .body(feil.message)

    @ExceptionHandler(KontaktinfoIngenTreff::class)
    fun kontaktInfoIngenTreff(feil: KontaktinfoIngenTreff) =
        ResponseEntity.status(NOT_FOUND)
            .body(feil.message)

    @ExceptionHandler(KontaktinfoUkjentFeil::class)
    fun kontaktInfoUkjentFeil(feil: KontaktinfoUkjentFeil) =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(feil.message)


    @ExceptionHandler(OppgaveAlleredeOpprettet::class)
    fun handleOppgaveAlleredeOpprettet(feil: OppgaveAlleredeOpprettet): ResponseEntity<Any> {
        LOG.warn(feil.message ?: "Uventet feil", feil)
        return ResponseEntity.status(FORBIDDEN)
            .body(feil.message)
    }

    @ExceptionHandler(HentIdenterException::class)
    fun handleHentIdenterException(feil: HentIdenterException) =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(feil.message)

    @ExceptionHandler(ManglendeBrukerInfoException::class)
    fun handleManglendeBrukerInfoException(feil: ManglendeBrukerInfoException) =
        ResponseEntity.status(UNAUTHORIZED)
            .body(feil.message)

    @ExceptionHandler(KanIkkeReaktiveresException::class)
    fun handleKanIkkeReaktiveresException(feil: KanIkkeReaktiveresException) =
        ResponseEntity.status(BAD_REQUEST)
            .body(feil.message)

    @ExceptionHandler(HentOppfolgingStatusException::class)
    fun handleHentOppfolgingStatusException(feil: HentOppfolgingStatusException) =
        ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(feil.message)

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(r: RuntimeException): ResponseEntity<Any> {
        LOG.error(r.message ?: "Uventet feil", r)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).build()
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<Any> {
        LOG.error("Requestbody matchet ikke endepunktets forventning", e)
        return ResponseEntity.badRequest().build()
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(t: Throwable): ResponseEntity<Any> {
        LOG.error("Uhåndtert feil oppsto", t)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).build()
    }


    companion object {
        private val LOG = loggerFor<FeilHandtering>()
    }
}

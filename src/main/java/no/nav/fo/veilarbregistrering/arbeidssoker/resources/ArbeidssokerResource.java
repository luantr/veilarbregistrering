package no.nav.fo.veilarbregistrering.arbeidssoker.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.fo.veilarbregistrering.arbeidssoker.ArbeidssokerService;
import no.nav.fo.veilarbregistrering.arbeidssoker.Arbeidssokerperiode;
import no.nav.fo.veilarbregistrering.bruker.Bruker;
import no.nav.fo.veilarbregistrering.bruker.BrukerAdapter;
import no.nav.fo.veilarbregistrering.bruker.Periode;
import no.nav.fo.veilarbregistrering.bruker.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/arbeidssoker")
@Produces("application/json")
@Api(value = "ArbeidssokerResource")
public class ArbeidssokerResource {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidssokerResource.class);

    private final ArbeidssokerService arbeidssokerService;
    private final UserService userService;
    private final VeilarbAbacPepClient pepClient;

    public ArbeidssokerResource(ArbeidssokerService arbeidssokerService, UserService userService, VeilarbAbacPepClient pepClient) {
        this.arbeidssokerService = arbeidssokerService;
        this.userService = userService;
        this.pepClient = pepClient;
    }

    @GET
    @Path("/perioder")
    @ApiOperation(value = "Henter alle perioder hvor bruker er registrert som arbeidssøker.")
    public ArbeidssokerperioderDto hentArbeidssokerperioder(
        @QueryParam("fraOgMed") LocalDate fraOgMed,
        @QueryParam("tilOgMed") LocalDate tilOgMed
    ) {
        LOG.info(String.format("hentArbeidssokerperioder med fraOgMed %s og tilOgMed %s", fraOgMed, tilOgMed));

        Bruker bruker = userService.hentBruker();

        LOG.info("Fant aktørId for bruker");

        pepClient.sjekkLesetilgangTilBruker(BrukerAdapter.map(bruker));

        LOG.info("Tilgang ok");

        List<Arbeidssokerperiode> arbeidssokerperiodes = arbeidssokerService.hentArbeidssokerperioder(
                bruker.getFoedselsnummer(), Periode.gyldigPeriode(fraOgMed, tilOgMed));

        LOG.info(String.format("Ferdig med henting av arbeidssokerperioder - fant %s perioder", arbeidssokerperiodes.size()));

        return map(arbeidssokerperiodes);
    }

    private ArbeidssokerperioderDto map(List<Arbeidssokerperiode> arbeidssokerperioder) {
        List<ArbeidssokerperiodeDto> arbeidssokerperiodeDtoer = arbeidssokerperioder.stream()
                .map(periode -> new ArbeidssokerperiodeDto(
                        periode.getPeriode().getFra(),
                        periode.getPeriode().getTil()))
                .collect(Collectors.toList());

        return new ArbeidssokerperioderDto(arbeidssokerperiodeDtoer);
    }

}

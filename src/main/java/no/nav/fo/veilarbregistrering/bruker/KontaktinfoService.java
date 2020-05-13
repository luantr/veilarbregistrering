package no.nav.fo.veilarbregistrering.bruker;

public class KontaktinfoService {

    private final PdlOppslagGateway pdlOppslagGateway;
    private final KrrGateway krrGateway;

    public KontaktinfoService(PdlOppslagGateway pdlOppslagGateway, KrrGateway krrGateway) {
        this.pdlOppslagGateway = pdlOppslagGateway;
        this.krrGateway = krrGateway;
    }

    public Kontaktinfo hentKontaktinfo(Bruker bruker) {
        //TODO: Her må vi implementere mer feilhåndtering - hva gjør vi hvis ett av kallene feiler?
        // Eller er det bedre at frontend gjør begge kallene selv?
        Person person = pdlOppslagGateway.hentPerson(bruker.getAktorId());

        return Kontaktinfo.of(
                krrGateway.hentKontaktinfo(bruker).getNummer().orElse(null),
                person.getTelefonnummer().orElse(null));
    }
}

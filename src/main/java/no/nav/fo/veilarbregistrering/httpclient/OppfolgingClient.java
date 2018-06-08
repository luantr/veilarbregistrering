package no.nav.fo.veilarbregistrering.httpclient;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.feil.Feil;
import no.nav.fo.veilarbregistrering.domain.AktivStatus;
import no.nav.fo.veilarbregistrering.domain.AktiverBrukerData;
import no.nav.fo.veilarbregistrering.domain.BrukerRegistrering;
import no.nav.sbl.rest.RestUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.COOKIE;
import static no.nav.sbl.rest.RestUtils.withClient;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Slf4j
public class OppfolgingClient {

    public static final String VEILARBOPPFOLGINGAPI_URL_PROPERTY_NAME = "VEILARBOPPFOLGINGAPI_URL";

    private final String baseUrl;
    private final SystemUserAuthorizationInterceptor systemUserAuthorizationInterceptor;
    private final Provider<HttpServletRequest> httpServletRequestProvider;

    @Inject
    public OppfolgingClient(Provider<HttpServletRequest> httpServletRequestProvider) {
        this(getRequiredProperty(VEILARBOPPFOLGINGAPI_URL_PROPERTY_NAME), new SystemUserAuthorizationInterceptor(), httpServletRequestProvider);
    }

    public OppfolgingClient(String baseUrl, SystemUserAuthorizationInterceptor systemUserAuthorizationInterceptor, Provider<HttpServletRequest> httpServletRequestProvider) {
        this.baseUrl = baseUrl;
        this.systemUserAuthorizationInterceptor = systemUserAuthorizationInterceptor;
        this.httpServletRequestProvider = httpServletRequestProvider;
    }

    public BrukerRegistrering aktiverBruker(AktiverBrukerData aktiverBrukerData) {
        BrukerRegistrering brukerRegistrering = new BrukerRegistrering();
        withClient(
                RestUtils.RestConfig.builder().build()
                , c -> postBrukerAktivering(aktiverBrukerData, c)
        );
        return brukerRegistrering;
    }

    private int postBrukerAktivering(AktiverBrukerData aktiverBrukerData, Client client) {
        String url = baseUrl + "/oppfolging/aktiverbruker";
        Response response = client.target(url)
                .register(systemUserAuthorizationInterceptor)
                .request()
                .post(Entity.json(aktiverBrukerData));

        int status = response.getStatus();

        if (status == 204) {
            return status;
        } else if (status == 500) {
            throw new WebApplicationException(response);
        } else {
            throw new RuntimeException("Uventet respons (" + status + ") ved aktivering av bruker mot " + url);
        }
    }

    public AktivStatus hentOppfolgingsstatus(String fnr) {
        String cookies = httpServletRequestProvider.get().getHeader(COOKIE);
        return getOppfolging(baseUrl + "/person/" + fnr + "/aktivstatus", cookies, AktivStatus.class);
    }

    private static <T> T getOppfolging(String url, String cookies, Class<T> returnType) {
        return Try.of(() -> withClient(c -> c.target(url).request().header(COOKIE, cookies).get(returnType)))
                .onFailure((e) -> {
                    log.error("Feil ved kall til Oppfølging {}", url, e);
                    throw new InternalServerErrorException();
                })
                .get();

    }
}
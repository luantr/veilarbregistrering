package no.nav.fo.veilarbregistrering.bruker.pdl;

import no.nav.fo.veilarbregistrering.bruker.*;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlGeografiskTilknytning;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlGtType;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlIdenter;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlAdressebeskyttelse;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlFoedsel;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlPerson;
import no.nav.fo.veilarbregistrering.bruker.pdl.endepunkt.PdlTelefonnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

class PdlOppslagMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PdlOppslagMapper.class);

    static GeografiskTilknytning map(PdlGeografiskTilknytning pdlGeografiskTilknytning) {
        if (pdlGeografiskTilknytning == null) {
            return null;
        }
        PdlGtType gtType = pdlGeografiskTilknytning.getGtType();
        switch (gtType) {
            case BYDEL:
                return GeografiskTilknytning.of(pdlGeografiskTilknytning.getGtBydel());
            case KOMMUNE:
                return GeografiskTilknytning.of(pdlGeografiskTilknytning.getGtKommune());
            case UTLAND:
                String gtLand = pdlGeografiskTilknytning.getGtLand();
                return gtLand != null ? GeografiskTilknytning.of(gtLand) : GeografiskTilknytning.ukjentBostedsadresse();
        }
        return null;
    }

    static Person map(PdlPerson pdlPerson) {
        return Person.of(
                pdlPerson.hoyestPrioriterteTelefonnummer()
                        .map(PdlOppslagMapper::map)
                        .orElse(null),
                pdlPerson.getSistePdlFoedsel()
                        .map(PdlOppslagMapper::map)
                        .orElse(null),
                pdlPerson.strengesteAdressebeskyttelse()
                        .map(PdlOppslagMapper::map)
                        .orElse(AdressebeskyttelseGradering.UKJENT));
    }

    private static Foedselsdato map(PdlFoedsel pdlFoedsel) {
        return Foedselsdato.of(pdlFoedsel.getFoedselsdato());
    }

    private static Telefonnummer map(PdlTelefonnummer pdlTelefonnummer) {
        return Telefonnummer.of(pdlTelefonnummer.getNummer(), pdlTelefonnummer.getLandskode());
    }

    static Identer map(PdlIdenter pdlIdenter) {
        return Identer.of(pdlIdenter.getIdenter().stream()
                .map(pdlIdent -> new Ident(
                        pdlIdent.getIdent(),
                        pdlIdent.getHistorisk(),
                        Gruppe.valueOf(pdlIdent.getGruppe().name())
                ))
                .collect(toList()));
    }

    protected static AdressebeskyttelseGradering map(PdlAdressebeskyttelse adressebeskyttelse) {
        if (adressebeskyttelse == null || adressebeskyttelse.getGradering() == null) {
            return AdressebeskyttelseGradering.UKJENT;
        }
        return AdressebeskyttelseGradering.valueOf(adressebeskyttelse.getGradering().name());
    }
}

package no.nav.fo.veilarbregistrering.bruker.pdl.hentIdenter;

import no.nav.fo.veilarbregistrering.bruker.pdl.PdlError;

import java.util.List;

public class PdlHentIdenterResponse {
    private PdlHentIdenter data;
    private List<PdlError> errors;

    public PdlHentIdenterResponse() {
    }

    public PdlHentIdenter getData() {
        return data;
    }

    public void setData(PdlHentIdenter data) {
        this.data = data;
    }

    public List<PdlError> getErrors() {
        return errors;
    }

    public void setErrors(List<PdlError> errors) {
        this.errors = errors;
    }
}

package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HentIdentForAktoerIdResponseTo {
    private final String ident;
    private final List<String> historiskeIdenter;

    public HentIdentForAktoerIdResponseTo(String ident, List<String> historiskeIdenter) {
        this.ident = ident;
        this.historiskeIdenter = new ArrayList<>(historiskeIdenter);
    }
}

package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Data
@Builder
public class PersistInngaaendeResponseTo {
    private String avsenderId;
    private String avsenderNavn;
    private String arkivSak;
    private String tittel;
    private String tema;
    private String brukerId;
    private List<DokumentinfoTo> dokumenter;
}

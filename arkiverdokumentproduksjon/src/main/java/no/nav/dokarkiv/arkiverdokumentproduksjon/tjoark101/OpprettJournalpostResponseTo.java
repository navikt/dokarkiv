package no.nav.service.dok.joark.nsb.to;

/**
 * The response object for the OpprettJournalpostService as a part in
 * arkiverDokumentproduksjon
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostResponseTo {

    private Long journalpostId;

    private Long dokumentInfoId;

    /**
     * Constructor with parameters
     *
     * @param journalpostId  The journalpostId
     * @param dokumentInfoId The dokumentInfoId
     */
    public OpprettJournalpostResponseTo(Long journalpostId,
                                        Long dokumentInfoId) {
        this.journalpostId = journalpostId;
        this.dokumentInfoId = dokumentInfoId;
    }

    public Long getJournalpostId() {
        return journalpostId;
    }

    public Long getDokumentInfoId() {
        return dokumentInfoId;
    }
}

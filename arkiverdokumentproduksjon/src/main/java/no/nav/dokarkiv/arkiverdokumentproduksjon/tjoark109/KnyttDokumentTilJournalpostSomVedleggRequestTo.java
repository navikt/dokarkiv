package no.nav.service.dok.joark.nsb.to;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class KnyttDokumentTilJournalpostSomVedleggRequestTo {

    private long knyttesFraJournalpostId;

    private long knyttesTilJournalpostId;

    private long dokumentInfoId;

    private String endretAvNavn;

    public long getKnyttesFraJournalpostId() {
        return knyttesFraJournalpostId;
    }

    public void setKnyttesFraJournalpostId(long knyttesFraJournalpostId) {
        this.knyttesFraJournalpostId = knyttesFraJournalpostId;
    }

    public long getKnyttesTilJournalpostId() {
        return knyttesTilJournalpostId;
    }

    public void setKnyttesTilJournalpostId(long knyttesTilJournalpostId) {
        this.knyttesTilJournalpostId = knyttesTilJournalpostId;
    }

    public long getDokumentInfoId() {
        return dokumentInfoId;
    }

    public void setDokumentInfoId(long dokumentInfoId) {
        this.dokumentInfoId = dokumentInfoId;
    }

    public String getEndretAvNavn() {
        return endretAvNavn;
    }

    public void setEndretAvNavn(String endretAvNavn) {
        this.endretAvNavn = endretAvNavn;
    }
}

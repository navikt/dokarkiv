package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import no.nav.service.dok.joark.nsb.to.KnyttDokumentTilJournalpostSomVedleggRequestTo;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public interface KnyttDokumentTilJournalpostSomVedleggValidator {

    void validate(KnyttDokumentTilJournalpostSomVedleggRequestTo request);
}

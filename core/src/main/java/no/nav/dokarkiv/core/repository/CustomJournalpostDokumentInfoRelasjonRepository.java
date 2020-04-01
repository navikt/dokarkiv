package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface CustomJournalpostDokumentInfoRelasjonRepository {
    Optional<JournalpostDokumentInfoRelasjon> findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(Long journalpostId, Long dokumentInfoId);
}

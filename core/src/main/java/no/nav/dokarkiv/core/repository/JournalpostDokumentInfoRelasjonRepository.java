package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {
	Optional<List<JournalpostDokumentInfoRelasjon>> findAllByJournalpostJournalpostId(Long journalpostId);

	Optional<List<JournalpostDokumentInfoRelasjon>> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);

}

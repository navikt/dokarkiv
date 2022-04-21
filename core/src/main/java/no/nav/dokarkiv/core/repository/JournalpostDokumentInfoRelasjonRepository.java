package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Skal ikke brukes noen andre steder enn i slett/logiskslett/hentjournalinfo tjenestene
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long>, CustomJournalpostDokumentInfoRelasjonRepository {
	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);
}

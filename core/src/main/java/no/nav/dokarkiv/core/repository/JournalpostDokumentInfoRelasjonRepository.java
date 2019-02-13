package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Skal ikke brukes noen andre steder enn i slett/logiskslett/hentjournalinfo tjenestene
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {

	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostIdAndTilknyttetJournalpostSom(Long journalpostId, TilknyttetJournalpostSomCode tilknyttetJournalpostSom);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);

	/**
	 * Denne metoden returnerer BigDecimal eller BigInteger så må returnere Object også konvertere til Long etterpå
	 */
	@Query(value = "select rel.DOKUMENT_INFO_ID from T_JP_DOK_INFO_REL rel where JOURNALPOST_ID=:journalpostId and SKJERMING_TYPE='POL'", nativeQuery = true)
	List<Object> findBegrensetRelasjonDokumentInfoIdByJournalpostId(@Param("journalpostId") Long journalpostId);

	Optional<JournalpostDokumentInfoRelasjon> findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(Long journalpostId, Long dokumentInfoId);
}

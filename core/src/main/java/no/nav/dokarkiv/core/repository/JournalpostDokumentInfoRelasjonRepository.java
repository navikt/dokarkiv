package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {
	Optional<List<JournalpostDokumentInfoRelasjon>> findAllByJournalpostJournalpostId(Long journalpostId);

	Optional<List<JournalpostDokumentInfoRelasjon>> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);

	@Query(value = "select DOKUMENT_INFO_ID from T_JP_DOK_INFO_REL rel where JOURNALPOST_ID=:journalpostId and exists (select * from T_BEGRENSNING where T_BEGRENSNING.JOURNALPOST_ID=rel.JOURNALPOST_ID and T_BEGRENSNING.DOKUMENT_INFO_ID=rel.DOKUMENT_INFO_ID and BEGRENSNING_TYPE='UTILGJENGELIGGJORT')", nativeQuery = true)
	Optional<List<BigInteger>> findBegrensetRelasjonDokumentInfoIdByJournalpostId(@Param("journalpostId") Long journalpostId);

	@Query(value = "select JOURNALPOST_ID from T_JP_DOK_INFO_REL rel where DOKUMENT_INFO_ID=:dokumentInfoId and exists (select * from T_BEGRENSNING where T_BEGRENSNING.JOURNALPOST_ID=rel.JOURNALPOST_ID and T_BEGRENSNING.DOKUMENT_INFO_ID=rel.DOKUMENT_INFO_ID and BEGRENSNING_TYPE='UTILGJENGELIGGJORT')", nativeQuery = true)
	Optional<List<BigInteger>> findBegrensetRelasjonJournalpostIdByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	Optional<List<JournalpostDokumentInfoRelasjon>> findAllByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(Long journalpostId, Long dokumentInfoId);
}

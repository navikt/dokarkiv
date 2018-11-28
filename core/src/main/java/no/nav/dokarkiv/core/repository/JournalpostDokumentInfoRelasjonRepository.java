package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.SkjermetVariant;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Skal ikke brukes noen andre steder enn i slett/logiskslett/hentjournalinfo tjenestene
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {

	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);

	/**
	 * Denne metoden returnerer BigDecimal eller BigInteger så må returnere Object også konvertere til Long etterpå
	 */
	@Query(value = "select rel.DOKUMENT_INFO_ID from T_JP_DOK_INFO_REL rel where JOURNALPOST_ID=:journalpostId and exists (select 'begrensning' from T_BEGRENSNING where T_BEGRENSNING.JOURNALPOST_ID=rel.JOURNALPOST_ID and T_BEGRENSNING.DOKUMENT_INFO_ID=rel.DOKUMENT_INFO_ID and BEGRENSNING_TYPE='UTILGJENGELIGGJORT')", nativeQuery = true)
	List<Object> findBegrensetRelasjonDokumentInfoIdByJournalpostId(@Param("journalpostId") Long journalpostId);

	Optional<JournalpostDokumentInfoRelasjon> findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(Long journalpostId, Long dokumentInfoId);

	/**
	 * Denne metoden returnerer BigDecimal eller BigInteger så må returnere Object også konvertere til Long etterpå
	 */
	@Query(value = "select rel.DOKUMENT_INFO_ID, fil.k_variant_format from T_JP_DOK_INFO_REL rel, T_FIL_DETALJER fil where rel.JOURNALPOST_ID=:journalpostId  and rel.dokument_info_id = fil.dokument_info_id and exists (select 'begrensning' from T_BEGRENSNING beg where beg.JOURNALPOST_ID=rel.JOURNALPOST_ID and beg.DOKUMENT_INFO_ID=rel.DOKUMENT_INFO_ID and beg.k_variant_format = fil.k_variant_format and BEGRENSNING_TYPE='SKJERMET')", nativeQuery = true)
	List<SkjermetVariant> findSkjermetRelasjonDokumentInfoIdAndVariantByJournalpostId(@Param("journalpostId") Long journalpostId);
}

package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface JoarkDeleteRepository extends Repository<Journalpost, Long> {

	@Modifying
	@Query(value = "delete from t_skannet_innhold where dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteSkannetInnholdByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_dokument_fil where fil_uuid in (select fil_uuid from t_fil_detaljer where dokument_info_id = :dokumentInfoId)", nativeQuery = true)
	void deleteDokumentFilByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_dokument_fil where fil_uuid in (select fil_uuid from t_fil_detaljer where dokument_info_id = :dokumentInfoId and k_variant_format = :variant_format)", nativeQuery = true)
	void deleteDokumentFilByDokumentInfoIdAndVariantFormat(@Param("dokumentInfoId") Long dokumentInfoId, @Param("variant_format") VariantFormatCode variantFormatCode);

	@Modifying
	@Query(value = "delete from t_fil_detaljer where dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteFilDetaljerByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_fil_detaljer where dokument_info_id = :dokumentInfoId and k_variant_format = :variant_format", nativeQuery = true)
	void deleteFilDetaljerByDokumentInfoIdAndVariantFormat(@Param("dokumentInfoId") Long dokumentInfoId, @Param("variant_format") VariantFormatCode variantFormatCode);

	@Modifying
	@Query(value = "delete from T_JP_DOK_INFO_REL where dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteDokInfoJPRelByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from T_JP_DOK_INFO_REL where journalpost_id = :journalpostId AND dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(@Param("journalpostId") Long journalpostid, @Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_dok_info_tillegg where dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteDokInfoTilleggByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_dokument_info where dokument_info_id = :dokumentInfoId", nativeQuery = true)
	void deleteDokInfoByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Modifying
	@Query(value = "delete from t_saksrelasjon where journalpost_id = :journalpostId", nativeQuery = true)
	void deleteSaksrelasjonByJournalpostId(@Param("journalpostId") Long journalpostId);

	@Modifying
	@Query(value = "delete from T_JP_TILLEGG where journalpost_id = :journalpostId", nativeQuery = true)
	void deleteJPTilleggByJournalpostId(@Param("journalpostId") Long journalpostId);

	@Modifying
	@Query(value = "delete from T_bruker where journalpost_id = :journalpostId", nativeQuery = true)
	void deleteBrukerByJournalpostId(@Param("journalpostId") Long journalpostId);

	@Modifying
	@Query(value = "delete from t_journalpost where journalpost_id = :journalpostId", nativeQuery = true)
	void deleteJournalpostByJournalpostId(@Param("journalpostId") Long journalpostId);
}

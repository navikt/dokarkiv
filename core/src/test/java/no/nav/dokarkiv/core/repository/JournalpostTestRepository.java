package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalpostTestRepository extends HibernateRepository<Journalpost>, BaseJpaTestRepository<Journalpost, Long> {

	@Query(value = "SELECT jt.journalpost_id FROM t_jp_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT JOURNALPOST_ID FROM t_jp_dok_info_rel j WHERE j.dokument_info_id = :dokumentInfoId", nativeQuery = true)
	List<Object> findAllJournalpostIdsByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Query(value = "SELECT max(jt.DOKUMENT_INFO_ID) FROM t_dok_info_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT jt.ORIG_JOURNALPOST_ID FROM T_DOKUMENT_INFO jt WHERE jt.DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	Long findJournalpostIdByDokumentinfoId(@Param("dokumentinfoId") String dokumentinfoId);

	Optional<Journalpost> findTopByKanalReferanseId(String kanalReferanseId);

	List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode);
}

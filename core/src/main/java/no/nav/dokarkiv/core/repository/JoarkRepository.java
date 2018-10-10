package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkRepository extends CrudRepository<Journalpost, Long> {

	@Query(value = "SELECT jt.journalpost_id FROM t_jp_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT * FROM t_journalpost j WHERE j.k_mottaks_kanal = :mottakskanal AND j.kanal_referanse_id = :kanalReferanseId", nativeQuery = true)
	Optional<Journalpost> findJournalpostIdByKanalReferanseIdAndMottakskanal(@Param("kanalReferanseId") String kanalReferanseId, @Param("mottakskanal") String mottakskanal);

	@Query(value = "SELECT journalpost_id FROM t_jp_dok_info_rel j WHERE j.dokument_info_id = :dokumentInfoId", nativeQuery = true)
	List<Long> findAllJournalpostIdsByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Query(value = "SELECT jt.DOKUMENT_INFO_ID FROM t_dok_info_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT jt.ORIG_JOURNALPOST_ID FROM T_DOKUMENT_INFO jt WHERE jt.DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	Long findJournalpostIdByDokumentinfoId(@Param("dokumentinfoId") String dokumentinfoId);

	@Query(value = "SELECT * FROM T_JOURNALPOST jp LEFT JOIN T_SAKSRELASJON sr ON jp.journalpost_id=sr.journalpost_id WHERE sr.SAK_NR_FK = :sakId AND sr.K_FAGSYSTEM = :fagsystem", nativeQuery = true)
	Optional<Journalpost> findJournalpostIdBySakIdAndFagsystem(@Param("sakId") String sakId, @Param("fagsystem") String dokumentinfoId);

	Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId);

	List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode);
}

package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface JoarkRepository extends CrudRepository<Journalpost, Long> {

	@Query(value = "SELECT jt.journalpost_id FROM t_jp_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT * from t_journalpost jp where jp.journalpost_id in ( select max(jt.journalpost_id) FROM t_jp_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi)", nativeQuery = true)
	Optional<Journalpost> findJournalpostByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT * FROM t_journalpost j WHERE j.k_mottaks_kanal = :mottakskanal AND j.kanal_referanse_id = :kanalReferanseId and rownum = 1", nativeQuery = true)
	Optional<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(@Param("kanalReferanseId") String kanalReferanseId, @Param("mottakskanal") String mottakskanal);

	@Query(value = "SELECT JOURNALPOST_ID FROM t_jp_dok_info_rel j WHERE j.dokument_info_id = :dokumentInfoId", nativeQuery = true)
	List<Object> findAllJournalpostIdsByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Query(value = "SELECT max(jt.DOKUMENT_INFO_ID) FROM t_dok_info_tillegg jt WHERE jt.nokkel = :nokkel AND jt.verdi = :verdi", nativeQuery = true)
	Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "SELECT jt.ORIG_JOURNALPOST_ID FROM T_DOKUMENT_INFO jt WHERE jt.DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	Long findJournalpostIdByDokumentinfoId(@Param("dokumentinfoId") String dokumentinfoId);

	@Query(value = "select j from Journalpost j join j.saksrelasjon s where s.sakId IN :sakId and s.fagsystem = :fagsystem")
	@EntityGraph(attributePaths = {"brukere", "tilleggsopplysninger", "journalpostDokumentInfoRelasjoner", "kryssreferanser", "behandlingsrelasjon",
			"journalpostDokumentInfoRelasjoner.dokumentInfo.tilleggsopplysninger", "journalpostDokumentInfoRelasjoner.dokumentInfo.skannetInnholdListe",
			"journalpostDokumentInfoRelasjoner.dokumentInfo.fildetaljerListe", "journalpostDokumentInfoRelasjoner.dokumentInfo.journalpostRelasjoner"})
	Optional<List<Journalpost>> findJournalposterBySakIdAndFagsystem(@Param("sakId") List<String> sakIdList, @Param("fagsystem") FagsystemCode fagsystemCode);

	@Query(value = "SELECT * FROM t_journalpost j WHERE j.k_journal_s IN ('M', 'MO') AND j.k_journalpost_t = 'I' AND j.dato_opprettet <= :tilOgMedDato", nativeQuery = true)
	Optional<List<Journalpost>> findUbehandledeJournalposts(@Param("tilOgMedDato") Date tilOgMedDato);

	@Query(value = "SELECT * FROM t_journalpost j WHERE j.k_journal_s IN ('M', 'MO') AND j.k_journalpost_t = 'I' AND j.dato_opprettet <= :tilOgMedDato AND j.K_FAGOMRADE in :temaListe", nativeQuery = true)
	Optional<List<Journalpost>> findUbehandledeJournalpostsWithTemaIn(@Param("tilOgMedDato") Date tilOgMedDato, @Param("temaListe") List<String> temaListe);

	Optional<Journalpost> findTopByKanalReferanseId(String kanalReferanseId);

	boolean existsByKanalReferanseId(String kanalReferanseId);

	List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode);
}

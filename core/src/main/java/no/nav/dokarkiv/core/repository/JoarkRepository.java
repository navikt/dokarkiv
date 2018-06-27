package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkRepository extends CrudRepository<Journalpost, Long> {

	@Query(value = "select jt.journalpost_id from t_jp_tillegg jt where jt.nokkel = :nokkel and jt.verdi = :verdi", nativeQuery = true)
	Optional<BigInteger> findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "select jt.DOKUMENT_INFO_ID from t_dok_info_tillegg jt where jt.nokkel = :nokkel and jt.verdi = :verdi", nativeQuery = true)
	Optional<BigInteger> findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);

	@Query(value = "select jt.ORIG_JOURNALPOST_ID from T_DOKUMENT_INFO jt where jt.DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	Optional<BigInteger> findJournalpostIdByDokumentinfoId(@Param("dokumentinfoId") String dokumentinfoId);

	//TODO OK?
	@Query(value = "delete * from T_JP_DOK_INFO_REL where JOURNALPOST_ID= :journalpostId", nativeQuery = true)
	void deleteJournalpostDokumentInfoRelasjon(@Param("journalpostId") Long journalpostId);

}

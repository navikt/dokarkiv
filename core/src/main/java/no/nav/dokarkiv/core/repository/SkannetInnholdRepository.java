package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface SkannetInnholdRepository extends CrudRepository<SkannetInnhold, Long> {

	@Modifying
	@Query(value = "DELETE from T_SKANNET_INNHOLD WHERE SKANNET_INNHOLD_ID = :skannetInnholdId AND DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	void deleteSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(@Param("skannetInnholdId") String skannetInnholdId, @Param("dokumentinfoId") String dokumentinfoId);

	@Query(value = "SELECT * from T_SKANNET_INNHOLD WHERE SKANNET_INNHOLD_ID = :skannetInnholdId AND DOKUMENT_INFO_ID = :dokumentinfoId", nativeQuery = true)
	Optional<SkannetInnhold> findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(@Param("skannetInnholdId") String skannetInnholdId, @Param("dokumentinfoId") String dokumentinfoId);
}

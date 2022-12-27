package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkannetInnholdRepository extends HibernateRepository<SkannetInnhold>, BaseJpaRepository<SkannetInnhold, Long> {
	/**
	 * Sletter SkannetInnhold basert på skannetInnholdId
	 * Bruker ikke Hibernate dirty checking siden det ikke behøves å sjekkes for cascade for denne operasjonen.
	 *
	 * @param skannetInnholdId id til SkannetInnhold
	 */
	@Modifying
	@Query(value = "delete from SkannetInnhold si where si.skannetInnholdId = :skannetInnholdId")
	void deleteBySkannetInnholdId(@Param("skannetInnholdId") Long skannetInnholdId);
}

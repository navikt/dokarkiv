package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.repository.projections.IdHolder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Utvidelse for {@link DokumentInfoRepository} for kode i test og prod.
 */
@NoRepositoryBean
public interface CustomDokumentInfoRepository extends Repository<DokumentInfo, Long> {
	@Query("""
			select new no.nav.dokarkiv.core.repository.projections.IdHolder(
			di.originalJournalpost.journalpostId
			)
			from DokumentInfo di
			where di.dokumentInfoId = :dokumentInfoId
			""")
	IdHolder findOriginalJournalpostIdByDokumentInfoId(Long dokumentInfoId);

	@Query("""
			select new no.nav.dokarkiv.core.repository.projections.IdHolder(
			max(di.dokumentInfoId)
			)
			from DokumentInfo di
			join di.tilleggsopplysninger dito
			where (key(dito) = :nokkel and dito = :verdi)
			""")
	IdHolder findDokumentInfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);
}

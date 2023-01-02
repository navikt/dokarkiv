package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JournalpostRepository extends HibernateRepository<Journalpost>, BaseJpaRepository<Journalpost, Long> {

	@Query(value = """
			select j from Journalpost j
			where j.journalstatus in (no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M, no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO)
			and j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I
			and j.changeStamp.createdDate <= :tilOgMedDato
			""")
	List<Journalpost> findUbehandledeJournalposts(@Param("tilOgMedDato") Date tilOgMedDato);

	@Query(value = """
			select j from Journalpost j
			where j.journalstatus in (no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M, no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO)
			and j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I
			and j.changeStamp.createdDate <= :tilOgMedDato AND j.fagomrade in :fagomrader
			""")
	List<Journalpost> findUbehandledeJournalpostsWithTemaIn(@Param("tilOgMedDato") Date tilOgMedDato, @Param("fagomrader") Set<FagomradeCode> fagomrader);

	Optional<Journalpost> findByKanalReferanseId(String kanalReferanseId);

	boolean existsByKanalReferanseId(String kanalReferanseId);
}

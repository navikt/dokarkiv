package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.projections.IdAndFagomradeHolder;
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

	//FIXME
	@Query(value = """
			select j from Journalpost j
			where j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I
			and j.journalstatus in (no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M, no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO)
			and j.changeStamp.createdDate <= :tilOgMedDato
			AND j.fagomrade in :fagomrader
			""")
	List<Journalpost> findUbehandledeJournalpostsWithTemaIn(@Param("tilOgMedDato") Date tilOgMedDato, @Param("fagomrader") Set<FagomradeCode> fagomrader);

	Optional<Journalpost> findByKanalReferanseId(String kanalReferanseId);

	boolean existsByKanalReferanseId(String kanalReferanseId);

	@Query(value = """
			select j
			from Journalpost j
			left join fetch j.saksrelasjon s
			left join fetch j.brukere b
			join fetch j.journalpostDokumentInfoRelasjoner jdir
			join fetch jdir.dokumentInfo d
			join fetch d.fildetaljerListe
			where j.journalpostId = :id
			""")
	Optional<Journalpost> fetchById(Long id);

	@Query(value = """
			select j
			from Journalpost j
			left join fetch j.saksrelasjon s
			where s.sakId in :sakIds
			""")
	List<Journalpost> fetchBySakIds(@Param("sakIds") List<Long> sakIds);

	@Query(value = """
			select j
			from Journalpost j
			left join fetch j.journalpostDokumentInfoRelasjoner jdir
			left join fetch jdir.dokumentInfo d
			left join fetch d.fildetaljerListe
			where j.journalpostId = :id
			""")
	Optional<Journalpost> fetchByIdWithJournalpostDokumentInfoRelasjoner(Long id);

	@Query(value = """
			select new no.nav.dokarkiv.core.repository.projections.IdAndFagomradeHolder(
			j.journalpostId, j.fagomrade			
			)
			from Journalpost j
			where j.journalpostId in :ids
			""")
	List<IdAndFagomradeHolder> findIdAndFagomradeByJournalpostIdIn(@Param("ids") List<Long> ids);

}

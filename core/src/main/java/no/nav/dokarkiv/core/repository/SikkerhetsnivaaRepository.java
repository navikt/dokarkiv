package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SikkerhetsnivaaRepository extends HibernateRepository<Journalpost>, BaseJpaRepository<Journalpost, Long> {


	@Query(value = """
			select j.journalpostId from Journalpost j
			where j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U
			and j.journalstatus = no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E
			and j.utsendingskanal = :utsendingskanal
			and j.lestDato is null
			and j.ekspedertDato >= :ekspedertFra
			and j.ekspedertDato <= :ekspedertTil
			and j.changeStamp.createdDate >= :datoOpprettetStart
			and j.changeStamp.createdDate <= :datoOpprettetSlutt
			""")
	List<Long> findULesteJournalposts(@Param("utsendingskanal") UtsendingsKanalCode utsendingskanal,
									  @Param("ekspedertFra") Date ekspedertFra,
									  @Param("ekspedertTil") Date ekspedertTil,
									  @Param ("datoOpprettetStart") Date datoOpprettetStart,
									  @Param("datoOpprettetSlutt") Date datoOpprettetSlutt);
}

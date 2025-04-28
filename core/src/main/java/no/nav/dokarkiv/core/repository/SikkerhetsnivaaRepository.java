package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SikkerhetsnivaaRepository extends HibernateRepository<Journalpost>, BaseJpaRepository<Journalpost, Long> {

	@Query(value = """
			select j.journalpostId
			from Journalpost j
			where j.journalpostId in(
				select j2.journalpostId
				from Journalpost j2
				join j2.saksrelasjon s
				where (j2.changeStamp.createdDate >= :datoOpprettetStart and j2.changeStamp.createdDate <= :datoOpprettetSlutt)
				and j2.utsendingskanal = :utsendingskanal
				and (s.feilregistrert is null or s.feilregistrert = false)
			)
			and j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U
			and j.journalstatus = no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E
			and j.lestDato is null
			and j.ekspedertDato >= :ekspedertFra
			and j.ekspedertDato <= :ekspedertTil
			and j.fagomrade <> no.nav.dokarkiv.core.domain.codes.FagomradeCode.STO
			""")
	List<Long>
	finnUlesteJournalposter(@Param("utsendingskanal") UtsendingsKanalCode utsendingskanal,
							@Param("ekspedertFra") LocalDateTime ekspedertFra,
							@Param("ekspedertTil") LocalDateTime ekspedertTil,
							@Param("datoOpprettetStart") LocalDateTime datoOpprettetStart,
							@Param("datoOpprettetSlutt") LocalDateTime datoOpprettetSlutt
	);
}

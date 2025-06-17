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
			join j.saksrelasjon s
			where j.utsendingskanal = :utsendingskanal
			and j.journalposttype = no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U
			and j.journalstatus = no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E
			and (j.ekspedertDato >= :ekspedertFra and j.ekspedertDato <= :ekspedertTil)
			and (j.changeStamp.createdDate >= :datoOpprettetStart and j.changeStamp.createdDate <= :datoOpprettetSlutt)
			and j.fagomrade <> no.nav.dokarkiv.core.domain.codes.FagomradeCode.STO
			and j.lestDato is null
			and (s.feilregistrert is null or s.feilregistrert = false)
			""")
	List<Long>
	finnUlesteJournalposter(@Param("utsendingskanal") UtsendingsKanalCode utsendingskanal,
							@Param("ekspedertFra") LocalDateTime ekspedertFra,
							@Param("ekspedertTil") LocalDateTime ekspedertTil,
							@Param("datoOpprettetStart") LocalDateTime datoOpprettetStart,
							@Param("datoOpprettetSlutt") LocalDateTime datoOpprettetSlutt
	);
}

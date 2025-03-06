package no.nav.dokarkiv.core.repository.projections;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public interface MottattJournalpostProjection {
	long getJournalpostId();

	String getJournalForendeEnhetId();

	String getFagomrade();

	String getJournalstatus();

	String getMottakskanal();

	String getBehandlingstema();

	@Value("#{target.changeStamp.createdDate}")
	Date getDatoOpprettet();
}

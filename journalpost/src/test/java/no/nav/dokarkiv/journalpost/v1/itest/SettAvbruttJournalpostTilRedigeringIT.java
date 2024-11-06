package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

public class SettAvbruttJournalpostTilRedigeringIT extends AbstractJournalpostIT {
	private final String SETTAVBRUTTJOURNALPOSTTILREDIGERBAR = "/settAvbruttJournalpostTilRedigering";

	@Test
	public void skalOppdatereAvbruttTilRedigerbart(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));

		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTTILREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertThat(response.getStatusCode()).isEqualTo(OK);




	}
}

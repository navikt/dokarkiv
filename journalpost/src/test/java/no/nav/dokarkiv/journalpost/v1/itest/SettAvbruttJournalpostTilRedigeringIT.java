package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class SettAvbruttJournalpostTilRedigeringIT extends AbstractJournalpostIT {
	private final String SETTAVBRUTTJOURNALPOSTREDIGERBAR = "/settAvbruttJournalpostRedigerbar";


	public Long setupAndReturnJournalpostId(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();
		return journalpostId;
	}

	@Test
	public void skalOppdatereAvbruttTilRedigerbart(){
		Long journalpostId = setupAndReturnJournalpostId();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));

		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();

		assertThat(oppdatertJournalpost.getJournalstatus()).isEqualTo(D);
		assertThat(oppdatertJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus()).isEqualTo(UNDER_REDIGERING);
	}

	@Test
	public void skalReturnereConflictVedUgyldigJournalstatus(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(D);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(response.getBody()).contains("Journalposten har feil status");

	}

	@Test
	public void skalReturnereBadRequestVedIkkeEksisterendeJournalpostId(){
		setupAndReturnJournalpostId();
		String feilJournalpostId = "300000003";

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + feilJournalpostId + SETTAVBRUTTJOURNALPOSTREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("ble ikke funnet");
	}

	@Test
	public void skalReturnereBadRequestVedManglendeHoveddokumentRelasjon(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		journalpost.clearJournalpostDokumentInfoRelasjoner();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("mangler Hoveddokumentrelasjon");
	}

	@Test
	public void skalReturnereUnauthorizedVedFeilClaimRole(){
		Long journalpostId = setupAndReturnJournalpostId();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("nei"));

		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}
}

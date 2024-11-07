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

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();

		assertThat(oppdatertJournalpost.getJournalstatus()).isEqualTo(D);
		assertThat(oppdatertJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus()).isEqualTo(UNDER_REDIGERING);
	}

	@Test
	public void feilmeldingVedUgyldigJournalstatus(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(D);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTTILREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(response.getBody()).contains("Journalposten har feil status");

	}

	@Test
	public void feilmeldingVedIkkeEksisterendeEllerFeilformatertJournalpostId(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		journalpostTestRepository.persist(journalpost);
		String feilJournalpostId = "300000003";

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + feilJournalpostId + SETTAVBRUTTJOURNALPOSTTILREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("ble ikke funnet");
	}

	@Test
	public void feilmeldingVedFeilformatertJournalpostId(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		journalpostTestRepository.persist(journalpost);
		String feilJournalpostId = "JPID200000001";

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + feilJournalpostId + SETTAVBRUTTJOURNALPOSTTILREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("må være et heltall.");
	}

	@Test
	public void feilmeldingVedManglendeHoveddokumentRelasjon(){
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(A);
		journalpost.clearJournalpostDokumentInfoRelasjoner();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + SETTAVBRUTTJOURNALPOSTTILREDIGERBAR, PUT, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("mangler Hoveddokumentrelasjon");
	}
}

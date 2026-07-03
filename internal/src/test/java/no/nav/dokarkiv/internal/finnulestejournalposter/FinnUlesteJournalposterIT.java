package no.nav.dokarkiv.internal.finnulestejournalposter;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.internal.AbstractInternalIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.SDP;
import static no.nav.dokarkiv.internal.finnulestejournalposter.FinnUlesteJournalposterController.SIKKERHETSNIVAA_ROLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class FinnUlesteJournalposterIT extends AbstractInternalIT {

	private static final int NOT_FOUND = 404;
	private static final int BAD_REQUEST = 400;
	private static final int EN_DAG = 1;
	private static final int FEM_DAGER = 5;
	private static final String AARSOPPGAVE_BREV_KODE = "MF_000053";
	private static final String FINNULESTEJOURNALPOSTER_PATH = "/rest/internal/finnUlesteJournalposter";

	// Skal finne uleste journalposter med utsendingskanal NAV_NO med ekspederttidspunkt i tidsintervallet [ekspedertFra, ekspedertTil] = [5 dager siden, 1 dager siden]
	@Test
	public void skalFinneUlesteJournalposter() {
		//Journalpost som skal bli plukket opp
		Journalpost aktuellUlestJournalpost = opprettUlestJournalpost(NAV_NO, 2, E, U);

		//Journalposter som ikke skal bli plukket opp
		Journalpost alleredeLest = opprettLestJournalpost();
		Journalpost feilKanal = opprettUlestJournalpost(SDP, 2, E, U);
		Journalpost forNyligEkspedert = opprettUlestJournalpost(NAV_NO, 0, E, U);
		Journalpost feilJournalstatus = opprettUlestJournalpost(NAV_NO, 2, FS, U);
		Journalpost feilJournalposttype = opprettUlestJournalpost(NAV_NO, 2, E, I);
		Journalpost feilregistrertSaksrelasjon = opprettUlestJournalpost(NAV_NO, 2, E, U);
		Journalpost ulestAarsoppgave = opprettUlestJournalpost(NAV_NO, 2, E, U, AARSOPPGAVE_BREV_KODE);
		feilregistrertSaksrelasjon.getSaksrelasjon().setFeilregistrert(true);

		journalpostTestRepository.persistAll(asList(alleredeLest, feilKanal, forNyligEkspedert, feilJournalstatus, feilJournalposttype, feilregistrertSaksrelasjon, ulestAarsoppgave));
		var ulestJournalpostId = journalpostTestRepository.persist(aktuellUlestJournalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim(SIKKERHETSNIVAA_ROLE));
		var response = restTemplate.exchange(buildUri(FINNULESTEJOURNALPOSTER_PATH, NAV_NO.toString(), 5, 1), GET, requestEntity, Long[].class);

		assertEquals(OK, response.getStatusCode());
		var ulesteJournalposter = response.getBody();
		assertThat(ulesteJournalposter)
				.singleElement()
				.satisfies(journalpost -> assertThat(journalpost).isEqualTo(ulestJournalpostId));
	}

	@ParameterizedTest
	@CsvSource(value = {
			"," + FEM_DAGER + "," + EN_DAG + "," + NOT_FOUND + "," + "Not Found", // utsendingskanalCode er null - gir 404 fra spring da det mangler en PathVariable
			"tull" + "," + FEM_DAGER + "," + EN_DAG + "," + BAD_REQUEST + "," + "tull er ikke en gyldig utsendingskanal", // utsendingskanal er ugyldig
			"NAV_NO" + "," + EN_DAG + "," + FEM_DAGER + "," + BAD_REQUEST + "," + "EkspedertFra kan ikke være før ekspedertTil" // ekspedertFra er før ekspedertTil
	})
	public void skalReturnereBadRequestForUgyldigInput(String utsendingsKanalCode, int ekspedertFra, int ekspedertTil, int expectedStatusCode, String feilmelding) {
		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim(SIKKERHETSNIVAA_ROLE));

		var response = restTemplate.exchange(buildUri(FINNULESTEJOURNALPOSTER_PATH, utsendingsKanalCode, ekspedertFra, ekspedertTil), GET, requestEntity, String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).contains(feilmelding);
	}

	@Test
	public void skalReturnereUnauthorizedHvisSikkerhetsnivaaRoleMangler() {
		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(buildUri(FINNULESTEJOURNALPOSTER_PATH, NAV_NO.name(), 5, 1), GET, requestEntity, String.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}

	private String buildUri(String path, String utsendingsKanalCode, int ekspedertFraDagerGamle, int ekspedertTilDagerGamle) {
		LocalDateTime now = LocalDateTime.now();
		String utsendingskanalUri = utsendingsKanalCode == null ? "" : utsendingsKanalCode;
		return path + "/" + utsendingskanalUri + "/" + now.minusDays(ekspedertFraDagerGamle) + "/" + now.minusDays(ekspedertTilDagerGamle);
	}

	private Journalpost opprettLestJournalpost() {
		Journalpost journalpost = generateBaseJp(2);
		journalpost.setLestDato(LocalDateTime.now().minusHours(5));
		return journalpost;
	}

	private Journalpost opprettUlestJournalpost(UtsendingsKanalCode kanalCode, int dagerSidenEkspedert, JournalStatusCode statusCode, JournalpostTypeCode journalpostTypeCode, String brevkode) {
		Journalpost journalpost = opprettUlestJournalpost(kanalCode, dagerSidenEkspedert, statusCode, journalpostTypeCode);
		journalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.forEach(jdir -> jdir.getDokumentInfo().setBrevkode(brevkode));
		return journalpost;
	}

	private Journalpost opprettUlestJournalpost(UtsendingsKanalCode kanalCode, int dagerSidenEkspedert, JournalStatusCode statusCode, JournalpostTypeCode journalpostTypeCode) {
		Journalpost jp = generateBaseJp(dagerSidenEkspedert);
		jp.setUtsendingskanal(kanalCode);
		jp.setJournalstatus(statusCode);
		jp.setJournalposttype(journalpostTypeCode);
		return jp;
	}

	private Journalpost generateBaseJp(int dagerSidenEkspedert) {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setKanalReferanseId(java.util.UUID.randomUUID().toString());
		journalpost.setEkspedertDato(LocalDateTime.now().minusDays(dagerSidenEkspedert));
		journalpost.setJournalstatus(E);
		journalpost.setLestDato(null);
		return journalpost;
	}
}

package no.nav.dokarkiv.sikkerhetsnivaa;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.journalpost.v1.itest.AbstractJournalpostIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.SDP;
import static no.nav.dokarkiv.sikkerhetsnivaa.JournalpostInternSikkerhetsnivaaController.SIKKERHETSNIVAA_PATH;
import static no.nav.dokarkiv.sikkerhetsnivaa.JournalpostInternSikkerhetsnivaaController.SIKKERHETSNIVAA_ROLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class FinnUlesteJournalposterIT extends AbstractJournalpostIT {

	private static final int NOT_FOUND = 404;
	private static final int BAD_REQUEST = 400;
	private static final int EN_DAG = 1;
	private static final int FEM_DAGER = 5;


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
		Journalpost ulestAarsoppgave = opprettUlestJournalpost(NAV_NO, 2, E, U, "Årsoppgave");
		feilregistrertSaksrelasjon.getSaksrelasjon().setFeilregistrert(true);

		journalpostTestRepository.persistAll(asList(alleredeLest, feilKanal, forNyligEkspedert, feilJournalstatus, feilJournalposttype, feilregistrertSaksrelasjon, ulestAarsoppgave));
		var ulestJournalpostId = journalpostTestRepository.persist(aktuellUlestJournalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim(SIKKERHETSNIVAA_ROLE));
		var response = restTemplate.exchange(buildUri(NAV_NO.toString(), 5, 1), GET, requestEntity, Long[].class);

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

		var response = restTemplate.exchange(buildUri(utsendingsKanalCode, ekspedertFra, ekspedertTil), GET, requestEntity, String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).contains(feilmelding);
	}

	@Test
	public void skalReturnereUnauthorizedHvisSikkerhetsnivaaRoleMangler() {
		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(buildUri(NAV_NO.name(), 5, 1), GET, requestEntity, String.class);

		assertEquals(response.getStatusCode(), UNAUTHORIZED);
	}

	private String buildUri(String utsendingsKanalCode, int ekspedertFraDagerGamle, int ekspedertTilDagerGamle) {
		LocalDateTime now = LocalDateTime.now();
		String utsendingskanalUri = utsendingsKanalCode == null ? "" : utsendingsKanalCode;
		return SIKKERHETSNIVAA_PATH + "/finnUlesteJournalposter/" + utsendingskanalUri + "/" + now.minusDays(ekspedertFraDagerGamle) + "/" + now.minusDays(ekspedertTilDagerGamle);
	}

	private Journalpost opprettLestJournalpost() {
		OffsetDateTime now = OffsetDateTime.now();
		Journalpost journalpost = generateBaseJp(2);
		journalpost.setLestDato(now.minusHours(5));
		return journalpost;
	}

	private Journalpost opprettUlestJournalpost(UtsendingsKanalCode kanalCode, int dagerSidenEkspedert, JournalStatusCode statusCode, JournalpostTypeCode journalpostTypeCode, String innhold) {
		Journalpost journalpost = opprettUlestJournalpost(kanalCode, dagerSidenEkspedert, statusCode, journalpostTypeCode);
		journalpost.setInnhold(innhold);
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
		OffsetDateTime now = OffsetDateTime.now();
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setKanalReferanseId(java.util.UUID.randomUUID().toString());
		journalpost.setEkspedertDato(now.minusDays(dagerSidenEkspedert));
		journalpost.setJournalstatus(E);
		journalpost.setLestDato(null);
		return journalpost;
	}
}

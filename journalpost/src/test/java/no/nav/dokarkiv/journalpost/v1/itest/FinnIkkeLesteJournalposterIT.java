package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.SDP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

public class FinnIkkeLesteJournalposterIT extends AbstractJournalpostIT {

	@Test
	public void happyPathFinnIkkeLesteJournalposter() {

		//Journalpost som skal bli plukket opp
		Journalpost ulestJournalpostNavNo = opprettUlestJournalpost(NAV_NO, 2, E, U);

		//Journalposter som ikke skal bli plukket opp
		//Journalposten har blitt lest
		Journalpost lestJournalpost = opprettLestJournalpost();
		//feil kanal
		Journalpost ulestJournalpostSDP = opprettUlestJournalpost(SDP, 2, E, U);
		//Ekspedert etter ekspedertTil
		Journalpost ulestJournalpostForNyligEkspedert = opprettUlestJournalpost(NAV_NO, 0, E, U);
		//Feil status FS, ikke E
		Journalpost ulestJournalpostFS = opprettUlestJournalpost(NAV_NO, 2, FS, U);
		//Feil journalposttype I, ikke U
		Journalpost ulestJournalpostTypeI = opprettUlestJournalpost(NAV_NO, 2, E, I);

		journalpostTestRepository.persistAll(Arrays.asList(lestJournalpost, ulestJournalpostSDP, ulestJournalpostForNyligEkspedert, ulestJournalpostFS, ulestJournalpostTypeI));
		long ulestJournalpostId = journalpostTestRepository.persist(ulestJournalpostNavNo).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<Long[]> response = restTemplate.exchange(buildUri(NAV_NO.toString(), 5, 1), GET, requestEntity, Long[].class);

		assertEquals(OK, response.getStatusCode());
		Long[] resultList = response.getBody();
		assertThat(resultList.length, is(1));
		assertThat(resultList[0], is(ulestJournalpostId));

	}

	private final int NOT_FOUND = 404;
	private final int BAD_REQUEST = 400;
	private final int EN_DAG = 1;
	private final int FEM_DAGER = 5;

	@ParameterizedTest
	@CsvSource(value = {
			"," + FEM_DAGER + "," + EN_DAG + "," + NOT_FOUND + "," + "Not Found", // utsendingskanalCode er null - gir 404 fra spring da det mangler en PathVariable
			"tull" + "," + FEM_DAGER + "," + EN_DAG + "," + BAD_REQUEST + "," + "utsendingskanal er ikke en gyldig utsendingskanal! Input utsendingskanal:tull", //utsendingskanal er ugyldig
			"NAV_NO" + "," + EN_DAG + "," + FEM_DAGER + "," + BAD_REQUEST + "," + "EkspedertFra kan ikke være før ekspedertTil"// ekspedertFra er før ekspedertTil
	})
	public void finnIkkeLesteJournalposterShouldGiveBadRequestWhenBadInput(String utsendingsKanalCode, int ekspedertFra, int ekspedertTil, int expectedStatusCode, String feilmelding) {
		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(buildUri(utsendingsKanalCode, ekspedertFra, ekspedertTil), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.valueOf(expectedStatusCode)));
		assertTrue(response.getBody().contains(feilmelding));
	}

	private String buildUri(String utsendingsKanalCode, int ekspedertFraDagerGamle, int ekspedertTilDagerGamle) {
		LocalDateTime now = LocalDateTime.now();
		String utsendingskanalUri = utsendingsKanalCode == null ? "" : utsendingsKanalCode;
		return URL_PROTECTED_INTERN + "/finnIkkeLesteJournalposter/" + utsendingskanalUri + "/" + now.minusDays(ekspedertFraDagerGamle) + "/" + now.minusDays(ekspedertTilDagerGamle);
	}

	private Journalpost opprettLestJournalpost() {
		OffsetDateTime now = OffsetDateTime.now();
		Journalpost journalpost = generateBaseJp(2);
		journalpost.setLestDato(now.minusHours(5));
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

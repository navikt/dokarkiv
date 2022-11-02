package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.DigitalPost;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.NavNoVarsel;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.Postadresse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OppdaterDistribusjonsinfoIT extends AbstractJournalpostIT {

	private static final String POSTKASSEADRESSE = "enadresse#1234";
	private static final String POSTKASSE_LEVERANDØR = "postkasseleverandør";

	@Test
	public void happyPathUpdateDistribusjonsinfo() {
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();

		performOppdaterDistribusjonsinfo(ferdigstiltJournalpost.getJournalpostId(), true, null);

		Journalpost ekspedertJournalpost = joarkRepository.findById(ferdigstiltJournalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(JournalStatusCode.E, ekspedertJournalpost.getJournalstatus());
		assertEquals(UtsendingsKanalCode.SDP, ekspedertJournalpost.getUtsendingskanal());
		assertNull(ekspedertJournalpost.getLestDato());
	}

	@Test
	public void happyPathUpdateDistribusjonsinfoSettLestDato() {
		var clock = Clock.fixed(Instant.now().minus(1, ChronoUnit.HOURS), ZoneId.systemDefault());
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();
		Long journalpostId = ferdigstiltJournalpost.getJournalpostId();

		performOppdaterDistribusjonsinfo(journalpostId, true, null);

		Journalpost ekspedertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(JournalStatusCode.E, ekspedertJournalpost.getJournalstatus());

		OffsetDateTime firstReadAtTimestamp = OffsetDateTime.now(clock);
		performOppdaterDistribusjonsinfo(journalpostId, false, firstReadAtTimestamp);

		OffsetDateTime secondReadAtTimestamp = OffsetDateTime.now(clock).plus(1, ChronoUnit.HOURS);
		performOppdaterDistribusjonsinfo(journalpostId, false, secondReadAtTimestamp);

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost2 = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost2.getUtsendingskanal());
		assertTrue(Duration.between(firstReadAtTimestamp.toInstant(), ferdigstiltJournalpost2.getLestDato().toInstant()).truncatedTo(ChronoUnit.SECONDS).isZero());

		TestTransaction.end();
	}

	@Test
	public void happyPathBulkUpdateDistribusjonsinfo() {
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();
		Long journalpostId = ferdigstiltJournalpost.getJournalpostId();

		OffsetDateTime ekspedertDato = OffsetDateTime.now();
		performBulkOppdaterDistribusjonsinfoAssertSuccess(createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
				.settStatusEkspedert(true).ekspedertDato(ekspedertDato)
				.digitalpostkasse(new DigitalPost(POSTKASSEADRESSE, POSTKASSE_LEVERANDØR)));

		Journalpost ekspedertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);
		assertEquals(JournalStatusCode.E, ekspedertJournalpost.getJournalstatus());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost2 = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost2.getUtsendingskanal());
		assertTrue(Duration.between(ekspedertDato.toInstant(), ferdigstiltJournalpost2.getEkspedertDato().toInstant()).truncatedTo(ChronoUnit.SECONDS).isZero());

		UtsendingsInfo utsendingsInfo = ferdigstiltJournalpost2.getUtsendingsInfo();
		assertNull(utsendingsInfo.getNavNoVarsling());
		assertNull(utsendingsInfo.getFysiskPostadresse());
		assertEquals(POSTKASSEADRESSE, utsendingsInfo.getDigitalPostadresse().getAdresse());
		assertEquals(POSTKASSE_LEVERANDØR, utsendingsInfo.getDigitalPostadresse().getPostkasseLeverandor());

		TestTransaction.end();
	}

	@Test
	public void happyPathBulkUpdateDistribusjonsinfoWithVolume() {
		OffsetDateTime ekspedertDato = OffsetDateTime.now();
		List<Journalpost> journalposts = IntStream.range(0, 40)
				.mapToObj(__ -> createJournalpost())
				.toList();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<Long> journalpostIds = journalposts.stream()
				.map(Journalpost::getJournalpostId)
				.map(this::ferdigstill)
				.map(Journalpost::getJournalpostId)
				.toList();

		performBulkOppdaterDistribusjonsinfoAssertSuccess(journalpostIds.stream()
				.map(journalpostId -> createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
						.settStatusEkspedert(true).ekspedertDato(ekspedertDato)
						.digitalpostkasse(new DigitalPost(POSTKASSEADRESSE, POSTKASSE_LEVERANDØR)))
				.toArray(JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder[]::new)
		);

		Journalpost ekspedertJournalpost = joarkRepository.findById(journalpostIds.get(0)).orElseThrow(RuntimeException::new);
		assertEquals(JournalStatusCode.E, ekspedertJournalpost.getJournalstatus());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost2 = joarkRepository.findById(journalpostIds.get(1)).orElseThrow(RuntimeException::new);

		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost2.getUtsendingskanal());
		assertTrue(Duration.between(ekspedertDato.toInstant(), ferdigstiltJournalpost2.getEkspedertDato().toInstant()).truncatedTo(ChronoUnit.SECONDS).isZero());

		UtsendingsInfo utsendingsInfo = ferdigstiltJournalpost2.getUtsendingsInfo();
		assertNull(utsendingsInfo.getNavNoVarsling());
		assertNull(utsendingsInfo.getFysiskPostadresse());
		assertEquals(POSTKASSEADRESSE, utsendingsInfo.getDigitalPostadresse().getAdresse());
		assertEquals(POSTKASSE_LEVERANDØR, utsendingsInfo.getDigitalPostadresse().getPostkasseLeverandor());

		TestTransaction.end();
	}
	@Test
	public void bulkUpdateDistribusjonsinfoShouldRejectMismatchingUtsendingskanal() {
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();
		Long journalpostId = ferdigstiltJournalpost.getJournalpostId();

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.S)
				.digitalpostkasse(new DigitalPost("enadresse#1234", "leverandør")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.NAV_NO)
				.digitalpostkasse(new DigitalPost("enadresse#1234", "leverandør")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
				.varsel(new NavNoVarsel("en indentifikator", "Hei hei, her er en melding.")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.S)
				.postadresse(null));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.NAV_NO)
				.varsel(null));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
				.digitalpostkasse(null));

		TestTransaction.start();

		Journalpost journalpostEtterOppdateringsforsok = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);
		assertNull(journalpostEtterOppdateringsforsok.getUtsendingskanal());

		TestTransaction.end();
	}

	@Test
	public void bulkUpdateDistribusjonsinfoShouldValidateUtsendingsinfos() {
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();
		Long journalpostId = ferdigstiltJournalpost.getJournalpostId();

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
				.digitalpostkasse(new DigitalPost(null, "leverandør")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.S)
				.postadresse(new Postadresse("gate gate", null, null, "1234", "agurk", "adfgh")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.NAV_NO)
				.varsel(new NavNoVarsel(null, "Hei hei, her er en melding.")));

		TestTransaction.start();

		Journalpost journalpostEtterOppdateringsforsok = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);
		assertNull(journalpostEtterOppdateringsforsok.getUtsendingskanal());

		TestTransaction.end();
	}

	@Test
	public void bulkUpdateDistribusjonsinfoShouldValidateBasicRequirementsJournalpost() {
		Journalpost ferdigstiltJournalpost = createFerdigstiltJournalpost();
		Long journalpostId = ferdigstiltJournalpost.getJournalpostId();

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.SDP)
				.settStatusEkspedert(null)
				.digitalpostkasse(new DigitalPost(null, "leverandør")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.S)
				.settStatusEkspedert(true).ekspedertDato(null)
				.postadresse(new Postadresse("gate gate", null, null, "1234", "agurk", "adfgh")));

		performBulkOppdaterDistribusjonsinfoAssertSuccess(0, 1, createJournalpostBulkPart(journalpostId, UtsendingsKanalCode.NAV_NO)
				.forsendelseId(null)
				.varsel(new NavNoVarsel(null, "Hei hei, her er en melding.")));

		TestTransaction.start();

		Journalpost journalpostEtterOppdateringsforsok = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);
		assertNull(journalpostEtterOppdateringsforsok.getUtsendingskanal());

		TestTransaction.end();
	}

	private JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder createJournalpostBulkPart(
			long journalpostId, UtsendingsKanalCode kanal) {
		return JournalpostWithDistribusjonsinfo.builder()
				.journalpostId(journalpostId)
				.forsendelseId(10_000L)
				.settStatusEkspedert(false)
				.utsendingsKanal(kanal.name());
	}

	private BulkOppdaterDistribusjonsinfoResponse performBulkOppdaterDistribusjonsinfoAssertSuccess(JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder... journalpostbuilders) {
		return performBulkOppdaterDistribusjonsinfoAssertSuccess(journalpostbuilders.length, 0, journalpostbuilders);
	}
	private BulkOppdaterDistribusjonsinfoResponse performBulkOppdaterDistribusjonsinfoAssertSuccess(int updated, int failed, JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder... journalpostbuilders) {
		BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = performBulkOppdaterDistribusjonsinfo(HttpStatus.OK, BulkOppdaterDistribusjonsinfoResponse.class, journalpostbuilders);
		assertEquals(updated, bulkOppdaterDistribusjonsinfoResponse.getJournalposter().getOppdatert() == null ? 0 : bulkOppdaterDistribusjonsinfoResponse.getJournalposter().getOppdatert().size());
		assertEquals(failed, bulkOppdaterDistribusjonsinfoResponse.getJournalposter().getFeilet() == null ? 0 : bulkOppdaterDistribusjonsinfoResponse.getJournalposter().getFeilet().size());
		return bulkOppdaterDistribusjonsinfoResponse;
	}

	private <T> T performBulkOppdaterDistribusjonsinfo(HttpStatus resultStatus, Class<T> responseClass, JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder... journalpostbuilders) {
		var journalposts = Stream.of(journalpostbuilders)
				.map(JournalpostWithDistribusjonsinfo.JournalpostWithDistribusjonsinfoBuilder::build)
				.collect(Collectors.toList());
		var bulkOppdaterDistribusjonsinfoEntity = new HttpEntity<>(new BulkOppdaterDistribusjonsinfoRequest(journalposts), createHeadersWithServiceUserToken());

		ResponseEntity<T> response = restTemplate.exchange(URL_BULK_DISTRIBUSJONSINFO_JOURNALPOST, HttpMethod.POST, bulkOppdaterDistribusjonsinfoEntity, responseClass);
		assertEquals(resultStatus, response.getStatusCode());

		return response.getBody();
	}

	private void performOppdaterDistribusjonsinfo(Long journalpostId, boolean settStatusEkspedert, OffsetDateTime readAtTimestamp) {
		var oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.utsendingsKanal(UtsendingsKanalCode.SDP.name())
				.settStatusEkspedert(settStatusEkspedert);
		if (readAtTimestamp != null) {
			oppdaterDistribusjonsinfoRequest.datoLest(readAtTimestamp);
		}
		var oppdaterDistribusjonsinfoEntity = new HttpEntity<>(oppdaterDistribusjonsinfoRequest.build(), createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + "/oppdaterDistribusjonsinfo", HttpMethod.PATCH, oppdaterDistribusjonsinfoEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private Journalpost createFerdigstiltJournalpost() {
		Journalpost journalpost = createJournalpost();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		return ferdigstill(journalpost.getJournalpostId());
	}

	private Journalpost ferdigstill(Long journalpostId) {
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();
		var finalizeRequestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> finalizeResponse = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, finalizeRequestEntity, String.class);
		assertEquals(HttpStatus.OK, finalizeResponse.getStatusCode());

		return joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);
	}

	private Journalpost createJournalpost() {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);
		return journalpost;
	}

}
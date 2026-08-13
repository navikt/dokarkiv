package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.repository.SlettebestillingTestRepository;
import no.nav.dokarkiv.core.util.TestdataFactory;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.util.TestdataFactory.getDokumentInfoFromJpDokInfoRelasjoner;
import static no.nav.dokarkiv.core.util.TestdataFactory.createGsak;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


public class SlettebestillingIT extends AbstractJournalpostIT {

	private static final String SLETTEBESTILLING_URL = "bestillSletting";
	private static final String OPPHEV_SLETTEBESTILLING_URL = "opphevBestillSletting";

	@Autowired
	private SlettebestillingTestRepository slettebestillingTestRepository;

	@AfterEach
	public void resetSlettebestillingDb() {
		if (!TestTransaction.isActive()) {
			TestTransaction.start();
		} else {
			TestTransaction.end();
			TestTransaction.start();
		}

		slettebestillingTestRepository.deleteAll();

		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	@Test
	public void shouldSuccesfullyCreateSeveralSlettebestillingSingleDocument() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request1 = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var requestEntity1 = new HttpEntity<>(request1, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result1 = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity1, String.class);
		assertThat(result1.getStatusCode()).isEqualTo(OK);
		assertThat(result1.getBody()).isNotNull();

		var request2 = new SlettebestillingRequest(ARK.name(), "her skal det slettes omigjen");
		var requestEntity2 = new HttpEntity<>(request2, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result2 = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity2, String.class);
		assertThat(result2.getStatusCode()).isEqualTo(OK);
		assertThat(result2.getBody()).isNotNull();

		var slettebestillingFromDb1 = slettebestillingTestRepository.findById(Long.valueOf(result1.getBody()));
		assertThat(slettebestillingFromDb1).isPresent();

		var slettebestillingFromDb2 = slettebestillingTestRepository.findById(Long.valueOf(result2.getBody()));
		assertThat(slettebestillingFromDb2).isPresent();

		assertThat(slettebestillingFromDb1.get().getDokumentInfoId()).isEqualTo(slettebestillingFromDb2.get().getDokumentInfoId());
	}

	@Test
	public void shouldFailIfNoDocument() {
		var slettebestillingIterator = slettebestillingTestRepository.findAll().iterator();
		assertThat(slettebestillingIterator.hasNext()).isFalse();
		var journalpostIterator = journalpostTestRepository.findAll().iterator();
		assertThat(journalpostIterator.hasNext()).isFalse();

		commitAndStartNewTransaction();

		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result = restTemplate.exchange(apiDokumentInfoPath("2", SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(result.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(result.getBody()).contains("ikke ble funnet");

		var slettebestillingsInDb = slettebestillingTestRepository.findAll();
		assertThat(slettebestillingsInDb).isEmpty();
	}

	@Test
	public void shouldOpphevBestillSletting() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();

		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var headers = createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId);
		var requestEntity = new HttpEntity<>(request, headers);
		var bestillResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(bestillResult.getStatusCode()).isEqualTo(OK);

		var opphevEntity = new HttpEntity<>(null, headers);
		var opphevResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), OPPHEV_SLETTEBESTILLING_URL), PATCH, opphevEntity, Void.class);
		assertThat(opphevResult.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		var slettebestilling = slettebestillingTestRepository.findById(Long.valueOf(bestillResult.getBody()));
		assertThat(slettebestilling).isPresent();
		assertEquals(AVBRUTT, slettebestilling.get().getSlettebestillingStatus());
	}

	@Test
	public void shouldReturnBadRequestWhenOpphevingFerdigstiltSlettebestilling() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var headers = createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId);
		var requestEntity = new HttpEntity<>(request, headers);
		var bestillResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(bestillResult.getStatusCode()).isEqualTo(OK);

		commitAndStartNewTransaction();

		var slettebestilling = slettebestillingTestRepository.findById(Long.valueOf(bestillResult.getBody()));
		assertThat(slettebestilling).isPresent();
		slettebestilling.get().endreSlettebestillingStatus(FERDIGSTILT, "itest");

		commitAndStartNewTransaction();

		var opphevEntity = new HttpEntity<>(null, headers);
		var opphevResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), OPPHEV_SLETTEBESTILLING_URL), PATCH, opphevEntity, String.class);

		assertThat(opphevResult.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(opphevResult.getBody()).contains("allerede er gjennomført");
	}

	@Test
	public void shouldReturnNotFoundWhenOpphevingAlreadyAvbruttSlettebestilling() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var headers = createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId);
		var requestEntity = new HttpEntity<>(request, headers);
		var bestillResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(bestillResult.getStatusCode()).isEqualTo(OK);

		var opphevEntity = new HttpEntity<>(null, headers);
		var firstOpphevResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), OPPHEV_SLETTEBESTILLING_URL), PATCH, opphevEntity, Void.class);
		assertThat(firstOpphevResult.getStatusCode()).isEqualTo(NO_CONTENT);

		var secondOpphevResult = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), OPPHEV_SLETTEBESTILLING_URL), PATCH, opphevEntity, String.class);
		assertThat(secondOpphevResult.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(secondOpphevResult.getBody()).contains("kunne avbrytes");
	}

	@Test
	public void shouldReturnBadRequestWhenBegrunnelseIsBlank() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest(ARK.name(), "   ");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(slettebestillingTestRepository.findAll()).isEmpty();
	}

	@Test
	public void shouldReturnBadRequestWhenHjemmelIsInvalid() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest("UGYLDIG_HJEMMEL", "gyldig begrunnelse");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(slettebestillingTestRepository.findAll()).isEmpty();
	}

	@Test
	public void shouldReturnUnauthorizedWhenTokenIsNotOnBehalfOf() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var requestEntity = new HttpEntity<>(request, createHeadersWithClientCredentialToken());

		var result = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(result.getBody()).contains("OIDC-token på Authorization-header må være et on behalf of-token");
		assertThat(slettebestillingTestRepository.findAll()).isEmpty();
	}

	@Test
	public void shouldReturnForbiddenWhenGruppenJoarkVedlikeholdIsMissing() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggForSakId(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		long dokumentInfoId = getDokumentInfoFromJpDokInfoRelasjoner(journalpost, 0).getDokumentInfoId();
		var request = new SlettebestillingRequest(ARK.name(), "her skal det slettes");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITH_GROUP_ACCESS, "FEIL_GRUPPE"));

		var result = restTemplate.exchange(apiDokumentInfoPath(String.valueOf(dokumentInfoId), SLETTEBESTILLING_URL), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(result.getBody()).contains("NAV-ansatt må ha gruppen med objectId=\\\"abcd163a-9821-4637-a23d-b706e5b24809\\\" i Entra ID token claims");
		assertThat(slettebestillingTestRepository.findAll()).isEmpty();
	}

}
package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.repository.SlettebestillingTestRepository;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.BEVARINGSTID;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.ENKELTSLETTING;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENTER_PA_SAK;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.core.util.TestdataFactory.createGsak;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class SlettebestillingIT extends AbstractJournalpostIT {

	private static final String SLETTEBESTILLING_URL = "bestillSletting";

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
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		var request1 = new SlettebestillingRequest(DOKUMENT.name(), journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId(), ENKELTSLETTING.name(), ARK.name(), "her skal det slettes");
		var requestEntity1 = new HttpEntity<>(request1, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result1 = restTemplate.exchange(apiPath(SLETTEBESTILLING_URL), POST, requestEntity1, String.class);
		assertThat(result1.getStatusCode()).isEqualTo(OK);
		assertThat(result1.getBody()).isNotNull();

		var request2 = new SlettebestillingRequest(DOKUMENT.name(), journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId(), ENKELTSLETTING.name(), ARK.name(), "her skal det slettes omigjen");
		var requestEntity2 = new HttpEntity<>(request2, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result2 = restTemplate.exchange(apiPath(SLETTEBESTILLING_URL), POST, requestEntity2, String.class);
		assertThat(result2.getStatusCode()).isEqualTo(OK);
		assertThat(result2.getBody()).isNotNull();

		var slettebestillingFromDb1 = slettebestillingTestRepository.findById(Long.valueOf(result1.getBody()));
		assertThat(slettebestillingFromDb1).isPresent();

		var slettebestillingFromDb2 = slettebestillingTestRepository.findById(Long.valueOf(result2.getBody()));
		assertThat(slettebestillingFromDb2).isPresent();

		assertThat(slettebestillingFromDb1.get().getDokumentInfoId()).isEqualTo(slettebestillingFromDb2.get().getDokumentInfoId());
	}

	@Test
	public void shouldRejectWhenSlettebestillingTypeIsNotDokument() {
		var sak = createGsak();
		sakTestRepository.persist(sak);
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(1L);
		journalpostTestRepository.persist(journalpost);

		commitAndStartNewTransaction();

		var request = new SlettebestillingRequest(DOKUMENTER_PA_SAK.name(), sak.getSakId(), BEVARINGSTID.name(), ARK.name(), "her skal det slettes");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:teamdokumenthandtering:system", MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result = restTemplate.exchange(apiPath(SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody()).contains("DOKUMENTER_PA_SAK er ikke en gyldig verdi");

		var slettebestillingsInDb = slettebestillingTestRepository.findAll();
		assertThat(slettebestillingsInDb).isEmpty();
	}

	@Test
	public void shouldFailIfNoDocument() {
		var slettebestillingIterator = slettebestillingTestRepository.findAll().iterator();
		assertThat(slettebestillingIterator.hasNext()).isFalse();
		var journalpostIterator = journalpostTestRepository.findAll().iterator();
		assertThat(journalpostIterator.hasNext()).isFalse();

		commitAndStartNewTransaction();

		var request = new SlettebestillingRequest(DOKUMENT.name(), 2L, ENKELTSLETTING.name(), ARK.name(), "her skal det slettes");
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken("itest:isa:gosys", MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		var result = restTemplate.exchange(apiPath(SLETTEBESTILLING_URL), POST, requestEntity, String.class);
		assertThat(result.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(result.getBody()).contains("ikke ble funnet");

		var slettebestillingsInDb = slettebestillingTestRepository.findAll();
		assertThat(slettebestillingsInDb).isEmpty();
	}
}

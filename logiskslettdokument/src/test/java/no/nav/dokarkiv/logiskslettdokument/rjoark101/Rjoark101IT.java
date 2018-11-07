package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void shouldAngreLogiskSlettDokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder().build());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.setSlettet(true);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		DokumentInfo angreLogiskSlettDokumentInfo = hentDokumentInfoEtterUtførtKall(journalpost);

		assertEquals(angreLogiskSlettDokumentInfo.getSlettet(), false);
	}

	@Test
	public void shouldFailToAngreLogiskSlettDokumentBecauseDocumentWasNotDeleted() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

		DokumentInfo dokumentInfoIkkeSlettet = hentDokumentInfoEtterUtførtKall(journalpost);

		assertThat(responseEntity.getBody(), containsString(
				String.format("kan ikke angre logisk sletting av dokument med dokumentInfoId=%s. Dokumentet er ikke logisk slettet",
						dokumentInfoIkkeSlettet.getDokumentInfoId())));
		assertEquals(dokumentInfoIkkeSlettet.getSlettet(), false);
	}

	private void setJournalpostSlettet(Journalpost journalpost) {
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.setSlettet(true);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		joarkRepository.save(journalpost);
	}

}

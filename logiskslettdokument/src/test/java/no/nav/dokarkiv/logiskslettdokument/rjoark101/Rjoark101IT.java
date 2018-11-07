package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.common.SlettemeldingsFunksjoner;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	private static String SLETTEMELDING = SlettemeldingsFunksjoner.getSlettemelding();

	@Test
	public void shouldAngreLogiskSlettDokument() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		setJournalpostSlettet(journalpost);

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
		assertThat(angreLogiskSlettDokumentInfo.getTittel(), not(endsWith(SLETTEMELDING)));
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

	@Test
	public void shouldFailToAngreLogiskSlettDokumentBecauseSlettetDokumentWithoutSlettemelding() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		setJournalpostSlettetWithoutSlettemelding(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		DokumentInfo angreLogiskSlettDokumentInfoWithoutSletteMelding = hentDokumentInfoEtterUtførtKall(journalpost);

		assertEquals(angreLogiskSlettDokumentInfoWithoutSletteMelding.getSlettet(), false);
		assertThat(angreLogiskSlettDokumentInfoWithoutSletteMelding.getTittel(), not(endsWith(SLETTEMELDING)));
	}


	private void setJournalpostSlettetWithoutSlettemelding(Journalpost journalpost) {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		joarkRepository.save(journalpost);
	}

	private void setJournalpostSlettet(Journalpost journalpost) {
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		SlettemeldingsFunksjoner.setDokumentLogiskSlettet(dokumentInfo);
		joarkRepository.save(journalpost);
	}

}

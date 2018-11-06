package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.common.SlettemeldingsFunksjoner;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void shouldAngreLogiskSlettDokument() {
		abacPermit();
		MDC.put(MDCConstants.MDC_USER_NAME, OPPRETTET_KILDE_NAVN);

		Journalpost journalpost = createJournalpostBuilder().build();
		setJournalpostSlettet(journalpost);
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<Begrensning> begrensetJp = hentJournalpostEtterUtførtKall (journalpost.getJournalpostId());
		assertEquals(begrensetJp.size(), 1L);
	}

	@Test
	public void shouldFailToAngreLogiskSlettDokumentBecauseDocumentWasNotDeleted() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");

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

		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke angre logisk sletting av journalpost med journalpostId=%s. Journalposten er ikke logisk slettet",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						journalpost.getJournalpostId())));


		List<Begrensning> begrensetJp = hentJournalpostEtterUtførtKall (journalpost.getJournalpostId());
		assertEquals(begrensetJp.size(), 0L);
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

		List<Begrensning> angreLogiskSlettJournalpostWithoutSletteMelding = hentJournalpostEtterUtførtKall(journalpost.getJournalpostId());

		assertEquals(angreLogiskSlettJournalpostWithoutSletteMelding.size(), 1l);
	}


	private void setJournalpostSlettetWithoutSlettemelding(Journalpost journalpost) {
		Begrensning jpBegrensning = Begrensning.builder().journalpost(journalpost).begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addBegrensning(jpBegrensning);
		joarkRepository.save(journalpost);
	}

	private void setJournalpostSlettet(Journalpost journalpost) {
		Begrensning jpBegrensning = Begrensning.builder().journalpost(journalpost).begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addBegrensning(jpBegrensning);
		joarkRepository.save(journalpost);
	}

}

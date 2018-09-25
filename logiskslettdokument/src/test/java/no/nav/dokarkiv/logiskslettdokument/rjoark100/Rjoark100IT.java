package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

public class Rjoark100IT extends AbstractSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldDeleteDocumentInJoark() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.PATCH, createHeaders(), LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get()
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseNoJournalpostDokumentInfoRelasjonFound() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		Long feilDokumentInfoId = journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId() + 13;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ feilDokumentInfoId, HttpMethod.PATCH, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("%s kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID), feilDokumentInfoId)));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseJournalpostIdAndDokumentInfoIdHasNoRelation() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.PATCH, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID), journalpost1.getJournalpostId(), journalpost2.findHoveddokumentDokumentInfoRelasjon()
						.getDokumentInfo()
						.getDokumentInfoId())));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseTooManyRelations() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = TestUtils.createJournalpostBuilder()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
						.build()).build();
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.PATCH, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("%s kan ikke slette dokument som har relasjoner med flere journalposter.",
				MDC.get(MDCConstants.MDC_REQUEST_ID))));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get()
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseDocumentAlreadyDeleted() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		dokumentinfoRepository.save(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.PATCH, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("%s kan ikke utføre logisk sletting av dokument med dokumentInfoId=%s. " +
				"Dokumentet er allerede logisk slettet", MDC.get(MDCConstants.MDC_REQUEST_ID), journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId())));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get()
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

}

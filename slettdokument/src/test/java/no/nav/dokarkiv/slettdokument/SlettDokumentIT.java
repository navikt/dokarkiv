package no.nav.dokarkiv.slettdokument;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.slettdokument.SlettDokumentRestController.REQUEST_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettdokument.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

public class SlettDokumentIT extends AbstractSlettDokumentIT {

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

		ResponseEntity<SlettDokumentResponse> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.DELETE, createHeaders(), SlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseNoJournalpostDokumentInfoRelasjonFound() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		Long feilDokumentInfoId = journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId() + 13;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ feilDokumentInfoId, HttpMethod.DELETE, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(REQUEST_ID + " kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=" + feilDokumentInfoId));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseJournalpostIdAndDokumentInfoIdHasNoRelation() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.DELETE, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(REQUEST_ID + " finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=" + journalpost1
				.getJournalpostId() +
				" og dokumentInfoId=" + journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseTooManyRelations() {
		abacPermit();

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
				.getDokumentInfoId(), HttpMethod.DELETE, createHeaders(), String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(REQUEST_ID + " kan ikke slette dokument som har relasjoner med flere journalposter."));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseDocumentAlreadyDeleted() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		dokumentinfoRepository.save(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.DELETE, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(REQUEST_ID + " har allerede slettet dokumentet med dokumentInfoId="
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

}

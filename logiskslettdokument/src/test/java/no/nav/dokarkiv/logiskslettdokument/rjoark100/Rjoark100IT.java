package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.common.SlettemeldingsFunksjoner;
import no.nav.dokarkiv.logiskslettdokument.util.TestUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark100IT extends AbstractSlettDokumentIT {

	private static String SLETTEMELDING = SlettemeldingsFunksjoner.getSlettemelding();

	@Test
	public void shouldDeleteDocumentInJoark() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		DokumentInfo logiskSlettetDokumentInfo = hentDokumentInfoEtterUtførtKall(journalpost);

		assertEquals(logiskSlettetDokumentInfo.getSlettet(), true);
		assertThat(logiskSlettetDokumentInfo.getTittel(), endsWith(SLETTEMELDING));
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

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + feilDokumentInfoId,
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
						feilDokumentInfoId)));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseJournalpostIdAndDokumentInfoIdHasNoRelation() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost2
						.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}


	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseTooManyRelations() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = TestUtils.createJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
								.build())
				.build();
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost1.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("kan ikke slette dokument som har relasjoner med flere journalposter."));

		DokumentInfo dokumentInfoMedForMangeRelasjoner = hentDokumentInfoEtterUtførtKall(journalpost1);

		assertEquals(dokumentInfoMedForMangeRelasjoner.getSlettet(), false);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseDocumentAlreadyDeleted() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		dokumentinfoRepository.save(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("kan ikke utføre logisk sletting av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk slettet",
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));

		DokumentInfo alleredeSlettetDokumentInfo = hentDokumentInfoEtterUtførtKall(journalpost);

		assertEquals(alleredeSlettetDokumentInfo.getSlettet(), true);
	}


	//TODO: Slett tester for -slettet
	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseTittelIsToLong() {
		abacPermit();

		String forLangTittel = "Dette er en tittel som ikke bare er lang, den er faktisk for lang. " +
				"Vi har valgt å gjøre denne tittelen lang fordi vi vill sikkerstille at vi ikke legger til suffikset " +
				"' - slettet' i sluttet av lange titler. Målet vi skal nå er straks over 490 tegn slik at vi vet at når " +
				"vi legger til slettemeldingen ' - slettet' i sluttet av strengen, så vil slettemeldingen med sin lengde " +
				"av ti tegn komme over den for det her testet magiske grensen på firehundreognittio tegn. " +
				"Nå så er vi akkurat ferdige.";

		Journalpost journalpost = TestUtils.createJournalpostBuilder().build();
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setTittel(forLangTittel);

		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/"
				+ journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), HttpMethod.PATCH, createHeaders(), LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		DokumentInfo dokumentInfoMedForLangTittel = hentDokumentInfoEtterUtførtKall(journalpost);

		assertEquals(dokumentInfoMedForLangTittel.getSlettet(), true);
		assertThat(dokumentInfoMedForLangTittel.getTittel(), not(endsWith(SLETTEMELDING)));
	}

}

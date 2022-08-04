package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for JournalpostDokumentInfoRelasjon.
 */
public class JournalpostDokumentInfoRelasjonTest {

	@Test
	public void shouldThrowExceptionForMissingTilknyttetAvNavn() {
		JournalpostDokumentInfoRelasjon relasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(relasjon, "tilknyttetAvNavn");
	}

	@Test
	public void shouldThrowExceptionForMissingTilknyttetJournalpostSom() {
		JournalpostDokumentInfoRelasjon relasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetAvNavn("Navn")
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(relasjon, "tilknyttetJournalpostSom");
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentInfoWhenEndeligJournalforing() {
		JournalpostDokumentInfoRelasjon relasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn("Navn")
				.build();
		relasjon.setJournalpost(getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
				.build());

		assertExceptionThrownWhenVerifyingMandatoryFields(relasjon, "dokumentInfo");
	}

	@Test
	public void shouldReturnTrueForNewRelasjonAndExistingDokumentInfo() {
		JournalpostDokumentInfoRelasjon relasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentInfoId(123L)
						.build())
				.build();

		assertThat(relasjon.isNewRelasjonToExistingDokumentInfo(), is(true));
	}

	@Test
	public void shouldReturnFalseForNewRelasjonAndNewDokumentInfo() {
		JournalpostDokumentInfoRelasjon relasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(getDokumentInfoBuilder()
						.build())
				.build();

		assertThat(relasjon.isNewRelasjonToExistingDokumentInfo(), is(false));
	}

	private void assertExceptionThrownWhenVerifyingMandatoryFields(JournalpostDokumentInfoRelasjon relasjon, String fieldName) {
		try {
			relasjon.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}

}

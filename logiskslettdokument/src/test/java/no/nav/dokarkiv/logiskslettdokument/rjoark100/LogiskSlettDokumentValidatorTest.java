package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.JournalpostDokumentInfoRelasjonNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link LogiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class LogiskSlettDokumentValidatorTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;


	@InjectMocks
	private LogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	//hoved-validering




	@Test
	public void shouldValidateJournalpostDokumentInfoRelasjoner() throws Exception {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost();

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();
		jpDokInfoRelasjoner.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo.getDokumentInfoId());
	}

	@Test
	public void shouldThrowExceptionIfJournalpostDokumentInfoRelasjonerFoundByDokumentInfoIdIsNull() throws Exception {
		thrown.expect(JournalpostDokumentInfoRelasjonNotFoundException.class);
		thrown.expectMessage(REQUEST_ID + " kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=" + DOKUMENTINFO_ID);

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> emptyJpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();

		validator.validateJournalpostDokumentInfoRelasjoner(emptyJpDokInfoRelasjoner, requestTo.getDokumentInfoId());
	}

	@Test
	public void shouldThrowExceptionIfTooManyRelations() {
		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
		thrown.expectMessage(REQUEST_ID + " kan ikke slette dokument som har relasjoner med flere journalposter. " +
				"DokumentinfoId=" + DOKUMENTINFO_ID + " har relasjoner med 3 journalposter.");

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		Journalpost journalpost1 = createJournalpost();
		Journalpost journalpost2 = getJournalpostBuilder()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
						.build()
				).build();
		Journalpost journalpost3 = getJournalpostBuilder()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
						.build()
				).build();


		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();
		jpDokInfoRelasjoner.addAll(journalpost1.getJournalpostDokumentInfoRelasjoner());
		jpDokInfoRelasjoner.addAll(journalpost2.getJournalpostDokumentInfoRelasjoner());
		jpDokInfoRelasjoner.addAll(journalpost3.getJournalpostDokumentInfoRelasjoner());

		validator.validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo.getDokumentInfoId());
	}


	@Test
	public void shouldValidateJournalpostIdBelongsToThisJournalpost() throws Exception {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost1 = createJournalpost();

		validator.validateJournalpostIdBelongsToThisJournalpost(journalpost1, requestTo);
	}

	@Test
	public void shouldFailToValidateJournalpostIfJournalpostIdDoesNotMatch() {
		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
		thrown.expectMessage(REQUEST_ID + " finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=500 og dokumentInfoId=" + DOKUMENTINFO_ID);

		LogiskSlettDokumentRequestTo feilRequestTo = createRequest(500L, DOKUMENTINFO_ID);
		Journalpost journalpost1 = createJournalpost();

		validator.validateJournalpostIdBelongsToThisJournalpost(journalpost1, feilRequestTo);
	}

	@Test
	public void shouldValidateSletteStatusForDokument() throws Exception {
		validator.validateDokumentIkkeLogiskSlettet(createDokumentInfo(false));
	}

	@Test
	public void shouldFailToValidateSletteStatusForDokument() {
		thrown.expect(DokumentAlleredeSlettetException.class);
		thrown.expectMessage(REQUEST_ID + " har allerede slettet dokumentet med dokumentInfoId=" + DOKUMENTINFO_ID);

		validator.validateDokumentIkkeLogiskSlettet(createDokumentInfo(true));
	}


	private LogiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId) {
		return new LogiskSlettDokumentRequestTo(journalpostId, dokumentInfoId);
	}

	private DokumentInfo createDokumentInfo(boolean sletteStatus) {
		return getDokumentInfoBuilder()
				.slettet(sletteStatus)
				.dokumentInfoId(DOKUMENTINFO_ID)
				.build();
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.dokumentInfo(createDokumentInfo(false))
						.build())
				.build();
	}

}
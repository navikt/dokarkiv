package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
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
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link LogiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class LogiskSlettDokumentValidatorTest {

	@InjectMocks
	private LogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldValidateLogiskSlettDokument() throws Exception {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();
		jpDokInfoRelasjoner.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostDokumentInfoRelasjonerFoundByDokumentInfoIdIsNull() throws Exception {
		thrown.expect(JournalpostDokumentInfoRelasjonNotFoundException.class);
		thrown.expectMessage(String.format("%s kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID), DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> emptyJpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(emptyJpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void shouldThrowExceptionIfTooManyRelations() {
		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
		thrown.expectMessage(String.format("%s kan ikke slette dokument som har relasjoner med flere journalposter. DokumentinfoId=%s har relasjoner med 3 journalposter.",
				MDC.get(MDCConstants.MDC_REQUEST_ID), DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		Journalpost journalpost1 = createJournalpost(DOKUMENTINFO_ID);
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

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void shouldFailToValidateJournalpostIfJournalpostIdDoesNotMatch() {
		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
		thrown.expectMessage(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=500 og dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID), DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo feilRequestTo = createRequest(500L, DOKUMENTINFO_ID);
		Journalpost journalpost1 = createJournalpost(DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();
		jpDokInfoRelasjoner.addAll(journalpost1.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, feilRequestTo);
	}

	@Test
	public void shouldFailToValidateSletteStatusForHovedDokument() throws DokumentAlleredeSlettetException {
		thrown.expect(DokumentAlleredeSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke utføre logisk sletting av journalpost med journalpostId=%s. " +
						"Journalposten er allerede logisk slettet",
				MDC.get(MDCConstants.MDC_REQUEST_ID), JOURNALPOST_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		Begrensning jpBegrensning = Begrensning.builder().journalpost(journalpost).begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).build();
		jpBegrensning.setOpprettetKildeNavn("OPPRETTET KILDE");
		journalpost.addBegrensning(jpBegrensning);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();
		jpDokInfoRelasjoner.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void shouldFailToValidateSletteStatusForVedlegg() throws DokumentAlleredeSlettetException {
		thrown.expect(DokumentAlleredeSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke utføre logisk sletting av dokument med dokumentInfoId=%s. " +
						"Dokumentet er allerede logisk slettet",
				MDC.get(MDCConstants.MDC_REQUEST_ID), 2L));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		DokumentInfo vedlegg = createDokumentInfo(2L);
		JournalpostDokumentInfoRelasjon rel = JournalpostDokumentInfoRelasjon.builder().dokumentInfo(vedlegg).journalpost(journalpost).tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).build();
		journalpost.addJournalpostDokumentInfoRelasjon(rel);
		Begrensning jpBegrensning = Begrensning.builder().dokumentInfo(vedlegg).begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).build();
		jpBegrensning.setOpprettetKildeNavn("OPPRETTET KILDE");
		vedlegg.addBegrensning(jpBegrensning);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = new ArrayList<>();
		jpDokInfoRelasjoner.add(rel);

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, requestTo);
	}

}
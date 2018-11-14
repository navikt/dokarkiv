package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
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

	@InjectMocks
	private LogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost_skalValidereOK() {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner =
				new ArrayList<JournalpostDokumentInfoRelasjon>(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void validerAtJournalpostDokumentInfoRelasjonerFinnes_derRelasjonerMangler_skalKasteJournalpostDokumentInfoRelasjonIkkeFunnetException() {
		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
		thrown.expectMessage(String.format("Kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
				DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> emptyJpDokInfoRelasjoner = new ArrayList<JournalpostDokumentInfoRelasjon>();

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(emptyJpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void validerAtJournalpostDokumentInfoRelasjonKunErKnyttetTilEnJournalpost_derDokumentInfoErKnyttetTilToJournalposter_skalKasteForMangeJournalpostDokumentInfoRelasjonErException() {
		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
		thrown.expectMessage(String.format("Kan ikke slette dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere journalposter", DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);

		Journalpost journalpost1 = createJournalpost(JOURNALPOST_ID);
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
	public void validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon_derInputIkkeSamsvarer_skalKasteIngenRelasjonMellomJournalpostIdOgDokumentInfoIdException() {
		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
		thrown.expectMessage(String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
				500L, DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo feilRequestTo = createRequest(500L, DOKUMENTINFO_ID);
		Journalpost journalpost1 = createJournalpost(DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner =
				new ArrayList<JournalpostDokumentInfoRelasjon>(journalpost1.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjoner, feilRequestTo);
	}

}
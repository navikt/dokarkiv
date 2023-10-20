package no.nav.dokarkiv.journalpost.v1.rjoark201;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FerdigstillJournalpostValidatorTest {

	private final FerdigstillJournalpostValidator validator = new FerdigstillJournalpostValidator();

	@Test
	public void shouldThrowExceptionIfJournalpoststatusIsNotMidlertidig() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.J);

		assertThrows(JournalpostIkkeMidlertidigException.class,
				() -> validator.validateJournalpostTilstand(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsFeilregistrert() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setFeilregistrert(Boolean.TRUE);

		assertThrows(JournalpostIkkeMidlertidigException.class,
				() -> validator.validateJournalpostTilstand(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoUnderRedigering() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		assertThrows(DokumentUnderRedigeringException.class,
				() -> validator.validateJournalpostTilstand(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfNotExactlyOneHoveddokument() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validateJournalpostStruktur(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatNotArkiv() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setVariantFormat(VariantFormatCode.SKANNING_META);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validateJournalpostStruktur(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfDuplicateFilinfoVariantFormat() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.addFilDetaljer(FilDetaljer.builder()
						.filtype(FilTypeCode.PDF)
						.variantFormat(VariantFormatCode.ARKIV)
						.filnavn("hello world")
						.filUuid("1337")
						.build());

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validateJournalpostStruktur(journalpost));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingInnhold() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setInnhold(null);

		var exception = assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost));
		assertTrue(exception.getMessage().contains("Journalpost.innhold"));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingFagomraade() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setFagomrade(null);

		var excpetion = assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost));
		assertTrue(excpetion.getMessage().contains("Journalpost.fagomrade"));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingAvsendMottaker() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setAvsenderMottaker(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Journalpost.avsendMottaker");
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissing() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setSaksrelasjon(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validateJournalpostStruktur(journalpost),
				"må ha en saksrelasjon");

	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingSaksnummer() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setSakId(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Saksrelasjon.sakId");
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingFagsystem() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setFagsystem(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Saksrelasjon.fagsystem");
	}

	@Test
	public void shouldThrowExceptionIfNoBrukerExists() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.clearBrukere();

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validateJournalpostStruktur(journalpost),
				"må knyttes til en bruker");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerId() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Bruker.brukerId");

	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerType() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Bruker.brukerType");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIsMissingTittel() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"DokumentInfo.tittel");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeAndMottakskanalIsMissing() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		journalpost.setMottakskanal(null);

		assertThrows(KanIkkeFerdigstilleException.class,
				() -> validator.validatePaakrevdeFelter(journalpost),
				"Journalpost.mottakskanal");
	}


	@Test
	public void shouldValidateJournalStatusOD() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.OD);
		journalpost.setJournalposttype(JournalpostTypeCode.I);

		validator.validateJournalpostTilstand(journalpost);
	}

	@Test
	public void shouldValidateJournalStatusFL() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FL);
		journalpost.setJournalposttype(JournalpostTypeCode.U);

		validator.validateJournalpostTilstand(journalpost);
	}

}
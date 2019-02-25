package no.nav.dokarkiv.ferdigstilljournalpost.v1.ferdigstill;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.context.junit.jupiter.DisabledIf;

public class JournalpostValidatorTest {

	private JournalpostValidator validator = new JournalpostValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldThrowExceptionIfJournalpoststatusIsNotMidlertidig() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.J);

		expectedException.expect(JournalpostIkkeMidlertidigException.class);

		validator.validateJournalpostTilstand(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsFeilregistrert() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setFeilregistrert(Boolean.TRUE);

		expectedException.expect(JournalpostIkkeMidlertidigException.class);

		validator.validateJournalpostTilstand(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoUnderRedigering() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		expectedException.expect(DokumentUnderRedigeringException.class);

		validator.validateJournalpostTilstand(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNotExactlyOneHoveddokument() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		expectedException.expect(KanIkkeFerdigstilleException.class);

		validator.validateJournalpostStruktur(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatNotArkiv() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setVariantFormat(VariantFormatCode.SKANNING_META);

		expectedException.expect(KanIkkeFerdigstilleException.class);

		validator.validateJournalpostStruktur(journalpost);
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

		expectedException.expect(KanIkkeFerdigstilleException.class);

		validator.validateJournalpostStruktur(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingInnhold() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setInnhold(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Journalpost.innhold");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingFagomraade() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setFagomrade(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Journalpost.fagomrade");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingAvsendMottaker() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.setAvsenderMottaker(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Journalpost.avsendMottaker");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingSaksnummer() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setSakId(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Saksrelasjon.sakId");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingFagsystem() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getSaksrelasjon().setFagsystem(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Saksrelasjon.fagsystem");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerId() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Bruker.brukerId");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerType() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("Bruker.brukerType");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIsMissingKategori() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("DokumentInfo.kategori");

		validator.validatePaakrevdeFelter(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIsMissingTittel() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		expectedException.expect(KanIkkeFerdigstilleException.class);
		expectedException.expectMessage("DokumentInfo.tittel");

		validator.validatePaakrevdeFelter(journalpost);
	}
}
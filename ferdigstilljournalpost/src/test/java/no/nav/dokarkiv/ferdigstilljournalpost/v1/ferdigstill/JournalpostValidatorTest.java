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

	// TODO: tester på manglende påkrevde felter

}
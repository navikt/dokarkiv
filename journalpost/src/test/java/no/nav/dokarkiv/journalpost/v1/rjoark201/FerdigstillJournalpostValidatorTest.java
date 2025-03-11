package no.nav.dokarkiv.journalpost.v1.rjoark201;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static java.lang.Boolean.TRUE;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SKANNING_META;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FerdigstillJournalpostValidatorTest {

	private final FerdigstillJournalpostValidator validator = new FerdigstillJournalpostValidator();

	@Test
	public void shouldThrowExceptionIfJournalpoststatusIsNotMidlertidig() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(J);

		assertThatExceptionOfType(JournalpostIkkeMidlertidigException.class)
				.isThrownBy(() -> validator.validateJournalpostTilstand(journalpost))
				.withMessageContaining("Den har journalstatus=J");
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsFeilregistrert() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getSaksrelasjon().setFeilregistrert(TRUE);

		assertThatExceptionOfType(JournalpostIkkeMidlertidigException.class)
				.isThrownBy(() -> validator.validateJournalpostTilstand(journalpost))
				.withMessageContaining("Den er feilregistrert");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoUnderRedigering() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumentstatus(UNDER_REDIGERING);

		assertThatExceptionOfType(DokumentUnderRedigeringException.class)
				.isThrownBy(() -> validator.validateJournalpostTilstand(journalpost))
				.withMessageContaining("Ett eller flere av dokumentene på journalposten er under redigering");
	}

	@Test
	public void shouldThrowExceptionIfNotExactlyOneHoveddokument() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setTilknyttetJournalpostSom(VEDLEGG);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validateJournalpostStruktur(journalpost))
				.withMessageContaining("Journalposten inneholder ingen eller flere enn ett hoveddokument");
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatNotArkiv() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setVariantFormat(SKANNING_META);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validateJournalpostStruktur(journalpost))
				.withMessageContaining("Journalposten mangler arkivvariant");
	}

	@Test
	public void shouldThrowExceptionIfDuplicateFilinfoVariantFormat() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.addFilDetaljer(FilDetaljer.builder()
						.filtype(PDF)
						.variantFormat(ARKIV)
						.filnavn("hello world")
						.filUuid("1337")
						.build());

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validateJournalpostStruktur(journalpost))
				.withMessageContaining("Journalposten inneholder flere dokumentvarianter med samme variantformat.");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingInnhold() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.setInnhold(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("tittel");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingFagomraade() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.setFagomrade(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("tema");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsMissingAvsendMottaker() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.setAvsenderMottaker(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("avsenderMottaker.navn");
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissing() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.setSaksrelasjon(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validateJournalpostStruktur(journalpost))
				.withMessageContaining("må ha en saksrelasjon");

	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingSaksnummer() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getSaksrelasjon().setSakId(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionIfSaksrelasjonIsMissingFagsystem() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getSaksrelasjon().setFagsystem(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionIfNoBrukerExists() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.clearBrukere();

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validateJournalpostStruktur(journalpost))
				.withMessageContaining("Journalposten er ikke knyttet til en bruker");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerId() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("bruker.id");

	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingBrukerType() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("bruker.idType");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIsMissingTittel() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("dokumenter.tittel");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeAndMottakskanalIsMissing() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(M);
		journalpost.setJournalposttype(I);
		journalpost.setMottakskanal(null);

		assertThatExceptionOfType(KanIkkeFerdigstilleException.class)
				.isThrownBy(() -> validator.validatePaakrevdeFelter(journalpost))
				.withMessageContaining("kanal");
	}


	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"M", "MO", "R","FL", "FS", "D", "A", "OD", "U", "UB"})
	public void shouldValidateJournalStatus(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(journalStatusCode);
		journalpost.setJournalposttype(I);

		validator.validateJournalpostTilstand(journalpost);
	}

	@Test
	public void shouldValidateJournalStatusFL() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(FL);
		journalpost.setJournalposttype(U);

		validator.validateJournalpostTilstand(journalpost);
	}

}
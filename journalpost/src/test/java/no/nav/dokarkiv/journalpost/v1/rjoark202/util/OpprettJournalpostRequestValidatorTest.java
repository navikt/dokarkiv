package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OpprettJournalpostRequestValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OpprettJournalpostRequest request;

	private OpprettJournalpostRequestValidator validator = new OpprettJournalpostRequestValidator();

	@Test
	public void happyPath() {
		request = createRequest(JournalpostType.INNGAAENDE);

		validator.validateRequest(request);
	}

	@Test
	public void shouldNotThrowExceptionIfAvsenderIsMissing() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(null)
						.id(null)
						.build())
				.build();

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderIsMissingNavn() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(null)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.navn");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderIdHasInvalidLength() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("***gammelt_fnr******gammelt_fnr******gammelt_fnr******gammelt_fnr***5678901")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderNameIsNotsetWhenAvsenderIdIsSet() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(null)
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfTemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, "tema", INNHOLD).build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfTittelIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, null).build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tittel");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.behandlingstema("behandlingstema")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("behandlingstema");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE, TEMA_FOR, INNHOLD)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE, TEMA_FOR, INNHOLD)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(null)
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfDokumentkategoriIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori("kategori")
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentkategori");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsNotSet() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(null)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.filtype");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype("filtype")
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.filtype");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsNotSet() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(null)
								.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.variantformat");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE, TEMA_FOR, INNHOLD)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat("variantformat")
								.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.variantformat");

		validator.validateRequest(request);
	}
}
package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
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
	public void shouldNotThrowExceptionIfMottakskanalTemaCombinationIsValid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SER)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		validator.validateRequest(request);
	}

	@Test
	public void shouldValidateWhenNoAvsenderMottaker() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(null)
				.build();

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdIsSetButNotIdType() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("***gammelt_fnr***")
						.idType(null)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.idType");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeIsSetAndNotId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndIdNot11Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1111111111a")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndMoreThan11Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("***gammelt_fnr***1")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdNot9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("NO7777777")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdMoreThan9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdNot9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1010101010")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRMoreThan9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.id(null)
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema("tema")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("behandlingstema")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("behandlingstema");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalTemaCombinationIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Det er kun mulig å arkivere med mottakskanal NAV_NO_UINNLOGGET dersom tema=SER.");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
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
	public void shouldThrowExceptionIfArkivsaksnummerNotNumeric() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer("quack123")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");

		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfDokumentkategoriIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
	public void shouldThrowExceptionIfFiltypeIsInvalidForARKIV() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_XML)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
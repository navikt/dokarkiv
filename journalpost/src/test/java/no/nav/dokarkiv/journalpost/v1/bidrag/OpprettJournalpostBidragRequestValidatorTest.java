package no.nav.dokarkiv.journalpost.v1.bidrag;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseBidragRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequestBidrag;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class OpprettJournalpostBidragRequestValidatorTest {

	private final OpprettJournalpostBidragRequestValidator opprettJournalpostBidragRequestValidator = new OpprettJournalpostBidragRequestValidator();

	@Test
	void shouldValidateRequest() {
		OpprettJournalpostRequest request = createRequestBidrag();
		opprettJournalpostBidragRequestValidator.validateRequest(request);
	}

	@Test
	void shouldThrowExceptionWhenAvsenderMottakerIdIsNull() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(FNR)
						.build())
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenAvsenderMottakerIdIsNot11Digits() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id("1111111111a")
						.idType(FNR)
						.build())
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenAvsenderMottakerIdTypeIsNotFnr() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id("999999999")
						.idType(ORGNR)
						.build())
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenDatoMottattIsNull() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.datoMottatt(null)
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenDokumentvariantNotArkiv() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel("Tullesøknad")
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenFiltypeNotPdf() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenFysiskDokumentIs0Length() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument("".getBytes())
										.build()))
								.build()))
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}

	@Test
	void shouldThrowExceptionWhenFysiskDokumentIsNull() {
		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(null)
										.build()))
								.build()))
				.build();
		assertThrows(InputValideringFeiletException.class, () ->
				opprettJournalpostBidragRequestValidator.validateRequest(request));
	}
}
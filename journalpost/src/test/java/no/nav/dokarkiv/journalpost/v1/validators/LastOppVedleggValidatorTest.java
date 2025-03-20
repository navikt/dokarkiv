package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DuplikatVedleggException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeLeggeTilVedleggException;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.util.TestDataGenerator.TITTEL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFilMedFilnavn;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostUnderArbeid;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN_VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createEnkelJournalpost;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class LastOppVedleggValidatorTest {

	private static final List<DokumentVariant> DOKUMENTVARIANTER = List.of(DokumentVariant.builder()
			.variantformat(VARIANTFORMAT_ARKIV)
			.filtype(FILTYPE_PDF)
			.fysiskDokument(FYSISK_DOKUMENT)
			.filnavn(FILNAVN_VEDLEGG)
			.build());
	private static final Dokument DOKUMENT = Dokument.builder()
			.tittel(TITTEL)
			.dokumentvarianter(DOKUMENTVARIANTER)
			.build();

	@Test
	void shouldValidateJournalpostAndDokument() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(D);

		assertDoesNotThrow(() -> LastOppVedleggValidator.validateJournalpostAndDokument(journalpost, DOKUMENT));
	}

	@Test
	void shouldNotValidateWhenRequestIsNull() {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateRequest(null))
				.withMessage("LastOppVedleggRequest kan ikke være null");
	}

	@ParameterizedTest
	@MethodSource
	void shouldNotValidateWhenDocumentIsInvalid(LastOppVedleggRequest request, String message) {
		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateRequest(request))
				.withMessage(message);
	}

	private static Stream<Arguments> shouldNotValidateWhenDocumentIsInvalid() {
		return Stream.of(
				Arguments.of(new LastOppVedleggRequest(null), "dokument kan ikke være null"),
				Arguments.of(new LastOppVedleggRequest(Dokument.builder().build()), "dokument.tittel kan ikke være tom eller null")
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldNotValidateWhenDocumentvariantIsInvalid(List<DokumentVariant> dokumentVariantList, String message) {
		var request = new LastOppVedleggRequest(Dokument.builder()
				.tittel(TITTEL)
				.dokumentvarianter(dokumentVariantList)
				.build());

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateRequest(request))
				.withMessage(message);
	}

	private static Stream<Arguments> shouldNotValidateWhenDocumentvariantIsInvalid() {
		return Stream.of(
				Arguments.of(null, "dokument.dokumentvarianter[] kan ikke være null eller en tom liste"),
				Arguments.of(List.of(DokumentVariant.builder().filtype(FILTYPE_PDF).build()),
						"dokument.dokumentvarianter[] sin dokumentvariant med filtype=PDF mangler variantformat"),
				Arguments.of(List.of(DokumentVariant.builder().variantformat(VARIANTFORMAT_ARKIV).build()),
						"dokument.dokumentvarianter[] sin dokumentvariant med variantformat=%s mangler filtype".formatted(VARIANTFORMAT_ARKIV)),
				Arguments.of(List.of(DokumentVariant.builder().variantformat(VARIANTFORMAT_ARKIV).filtype(FILTYPE_PDF).build()),
						"dokument.dokumentvarianter[] sin dokumentvariant med variantformat=%s mangler fysisk dokument".formatted(VARIANTFORMAT_ARKIV)),
				Arguments.of(List.of(DokumentVariant.builder().variantformat(VARIANTFORMAT_ARKIV).filtype(FILTYPE_PDF).fysiskDokument("fil".getBytes()).build()),
						"dokument.dokumentvarianter[] sin dokumentvariant med variantformat=%s mangler filnavn".formatted(VARIANTFORMAT_ARKIV))
		);
	}

	@ParameterizedTest
	@EnumSource(value = VariantFormatCode.class)
	void shouldNotValidateWhenRequestContainsDuplicateVariantformat(VariantFormatCode variantformat) {
		var request = new LastOppVedleggRequest(Dokument.builder()
				.tittel(TITTEL)
				.dokumentvarianter(List.of(
						DokumentVariant.builder()
								.variantformat(variantformat.name())
								.filtype(FILTYPE_PDF)
								.fysiskDokument(FYSISK_DOKUMENT)
								.filnavn(FILNAVN_VEDLEGG)
								.build(),
						DokumentVariant.builder()
								.variantformat(variantformat.name())
								.filtype(FILTYPE_PDF)
								.fysiskDokument(FYSISK_DOKUMENT)
								.filnavn(FILNAVN_VEDLEGG)
								.build()))
				.build());

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateRequest(request))
				.withMessage("dokument.dokumentvarianter[] inneholder mer enn én dokumentvariant med følgende variantformat(er): %s",
						List.of(variantformat.name()));
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"D"}, mode = EXCLUDE)
	void shouldNotValidateJournalpostAndDokumentWhenJournalpoststatusIsNotUnderProduksjon(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createEnkelJournalpost();
		journalpost.setJournalstatus(journalStatusCode);

		assertThatExceptionOfType(KanIkkeLeggeTilVedleggException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateJournalpostAndDokument(journalpost, DOKUMENT))
				.withMessage("Journalposten har status=%s, men må ha status=%s",
						journalStatusCode,
						D.name());
	}

	@Test
	void shouldNotValidateJournalpostAndDokumentWhenJournalpostDoesNotHaveHoveddokument() {
		Journalpost journalpost = createEnkelJournalpost(D, I);

		assertThatExceptionOfType(KanIkkeLeggeTilVedleggException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateJournalpostAndDokument(journalpost, DOKUMENT))
				.withMessage("Journalposten må et hoveddokument");
	}

	@Test
	void shouldNotValidateJournalpostAndDokumentWhenDuplicateVedleggExists() {
		DokumentInfo vedlegg1 = createVedlegg(1L, null);
		DokumentInfo vedlegg2 = createVedlegg(2L, FILNAVN_VEDLEGG);
		DokumentInfo vedlegg3 = createVedlegg(3L, FILNAVN_VEDLEGG);

		Journalpost journalpost = createJournalpostUnderArbeid();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, vedlegg1));
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, vedlegg2));
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, vedlegg3));

		assertThatExceptionOfType(DuplikatVedleggException.class)
				.isThrownBy(() -> LastOppVedleggValidator.validateJournalpostAndDokument(journalpost, DOKUMENT))
				.hasFieldOrPropertyWithValue("dokumentInfoId", 2L);
	}

	private static DokumentInfo createVedlegg(long dokumentInfoId, String filnavn) {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentInfoId(dokumentInfoId);
		dokumentInfo.clearFildetaljerListe();

		FilDetaljer filDetaljer = createFildetaljerOgFilMedFilnavn(dokumentInfo, ARKIV, filnavn);
		dokumentInfo.addFilDetaljer(filDetaljer);
		return dokumentInfo;
	}

	@ParameterizedTest
	@EnumSource(value = VariantFormatCode.class, names = {"ARKIV"}, mode = EXCLUDE)
	void shouldValidateJournalpostAndDokumentWhenDuplicateNonArkivvariantVedleggExists() {
		DokumentInfo vedlegg = createDokumentInfo();
		vedlegg.clearFildetaljerListe();

		FilDetaljer filDetaljer = createFildetaljerOgFilMedFilnavn(vedlegg, PRODUKSJON, FILNAVN_VEDLEGG);
		vedlegg.addFilDetaljer(filDetaljer);

		Journalpost journalpost = createJournalpostUnderArbeid();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, vedlegg));

		assertDoesNotThrow(() -> LastOppVedleggValidator.validateJournalpostAndDokument(journalpost, DOKUMENT));
	}

}
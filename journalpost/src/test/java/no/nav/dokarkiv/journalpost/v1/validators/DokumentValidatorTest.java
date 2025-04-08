package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.knytttilannensak.DokumentUtilsTest.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class DokumentValidatorTest {

	private Dokument.DokumentBuilder dokumentBuilder;

	@BeforeEach
	void setup() {
		dokumentBuilder = Dokument.builder()
				.dokumentKategori(DOKUMENTKATEGORI_SED)
				.tittel(DOKUMENT_TITTEL1)
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_PDF)
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()));
	}

	@Test
	void shouldValidateDokumentWithDifferentVariantformat() {
		var dokument = dokumentBuilder
				.dokumentvarianter(List.of(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.fysiskDokument(FYSISK_DOKUMENT)
								.build(),
						DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.fysiskDokument(FYSISK_DOKUMENT)
								.variantformat(VARIANTFORMAT_ORIGINAL)
								.build()))
				.build();

		assertDoesNotThrow(() -> DokumentValidator.validateDokument(0, dokument));
	}

	@Test
	void shouldThrowExceptionWhenSkjultTittel() {
		var dokument = dokumentBuilder
				.tittel(SKJULT_TITTEL)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].tittel kan ikke være " + SKJULT_TITTEL);
	}

	@ParameterizedTest
	@MethodSource
	void shouldThrowExceptionWhenDokumentkategoriIsInvalid(Integer dokumentIdx, String feilmelding) {
		var dokument = dokumentBuilder
				.dokumentKategori(DOKUMENTKATEGORI_UGYLDIG)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(dokumentIdx, dokument))
				.withMessage(feilmelding);
	}

	private static Stream<Arguments> shouldThrowExceptionWhenDokumentkategoriIsInvalid() {
		final String feilmelding = "%s.dokumentKategori validerer ikke mot kodeverk. Gyldige verdier for dokumentKategori er %s. Mottatt dokumentKategori=%s";
		final String dokumentKategoriKoder = Arrays.toString(DokumentKategoriCode.values());

		return Stream.of(
				Arguments.of(0, feilmelding.formatted("dokumenter[0]", dokumentKategoriKoder, DOKUMENTKATEGORI_UGYLDIG)),
				Arguments.of(null, feilmelding.formatted("dokumenter[0]", dokumentKategoriKoder, DOKUMENTKATEGORI_UGYLDIG))
		);
	}

	@Test
	void shouldThrowExceptionWhenDokumentHasNoVariantformat() {
		var dokument = dokumentBuilder
				.dokumentvarianter(null)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("Alle dokumenter må innholde en dokumentvariant av typen ARKIV");
	}

	@Test
	void shouldThrowExceptionWhenVariantformatArkivIsMissing() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_XML)
						.fysiskDokument(FYSISK_DOKUMENT)
						.variantformat(VARIANTFORMAT_ORIGINAL)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("Alle dokumenter må innholde en dokumentvariant av typen ARKIV. %s inneholder følgende varianter: %s",
						DOKUMENT_TITTEL1, VARIANTFORMAT_ORIGINAL);
	}

	@Test
	void shouldThrowExceptionWhenVariantformatIsDuplicate() {
		var dokument = dokumentBuilder
				.dokumentvarianter(List.of(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.fysiskDokument(FYSISK_DOKUMENT)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build(),
						DokumentVariant.builder()
								.filtype(FILTYPE_XML)
								.fysiskDokument(FYSISK_DOKUMENT)
								.variantformat(VARIANTFORMAT_ORIGINAL)
								.build(),
						DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.fysiskDokument(FYSISK_DOKUMENT)
								.variantformat(VARIANTFORMAT_ORIGINAL)
								.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: variantformat=%s funnet 2 ganger",
						DOKUMENT_TITTEL1, VARIANTFORMAT_ORIGINAL);
	}

	@Test
	void shouldThrowExceptionWhenFiltypeIsNotSet() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(null)
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].filtype må være satt for variantformat=ARKIV");
	}

	@Test
	void shouldThrowExceptionWhenFiltypeIsInvalid() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_UGYLDIG)
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].filtype validerer ikke mot kodeverk for variantformat=ARKIV. Gyldige verdier for filtype er %s. Mottatt filtype=%s",
						Arrays.toString(FilTypeCode.values()), FILTYPE_UGYLDIG);
	}

	@Test
	void shouldThrowExceptionWhenVariantformatIsNotSet() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_PDF)
						.variantformat(null)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].variantformat må være satt");
	}

	@Test
	void shouldThrowExceptionWhenVariantformatIsInvalid() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_PDF)
						.variantformat(VARIANTFORMAT_UGYLDIG)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].variantformat validerer ikke mot kodeverk. Gyldige verdier for variantformat er %s. Mottatt variantformat=%s",
						Arrays.toString(VariantFormatCode.values()), VARIANTFORMAT_UGYLDIG, VARIANTFORMAT_UGYLDIG);
	}

	@ParameterizedTest
	@EnumSource(value = FilTypeCode.class, names = {"PDF", "PDFA", "XLSX"}, mode = EXCLUDE)
	void shouldThrowExceptionWhenFiltypeIsInvalidForARKIV(FilTypeCode filTypeCode) {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(filTypeCode.name())
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].filtype må være PDF, PDFA eller XLSX for variantformat=ARKIV. Mottatt filtype=%s",
						filTypeCode.name());
	}

	@ParameterizedTest
	@NullAndEmptySource
	void shouldThrowExceptionWhenFysiskDokumentNullOrEmpty(byte[] fysiskDokument) {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_PDF)
						.fysiskDokument(fysiskDokument)
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].fysiskDokument for variantformat=ARKIV må være en base64 representert fil større enn 0 bytes");
	}

	@Test
	void shouldThrowExceptionWhenFysiskDokumentContainsInvalidMagicNumber() {
		var dokument = dokumentBuilder
				.dokumentvarianter(singletonList(DokumentVariant.builder()
						.filtype(FILTYPE_PDF)
						.fysiskDokument(FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER)
						.variantformat(VARIANTFORMAT_ARKIV)
						.build()))
				.build();

		assertThatExceptionOfType(InvalidPdfException.class)
				.isThrownBy(() -> DokumentValidator.validateDokument(0, dokument))
				.withMessage("dokumenter[0].dokumentvarianter[].fysiskDokument med variantformat=ARKIV kan ikke lagres i fagarkivet. fysiskDokument magicNumber={FF D8 FF E0 00} matcher ikke angitt filtype=PDF");
	}
}
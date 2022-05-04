package no.nav.dokarkiv.arkivervariant.rjoark103;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArkiverVariantValidatorTest {

	private ArkiverVariantValidator validator = new ArkiverVariantValidator();

	@Test
	public void happyPath() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET)
				.filType(FilTypeCode.PDF).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void dokumentIdIsNull() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET)
				.filType(FilTypeCode.PDF).build();

		assertThrows(UgyldigInputException.class, () ->
				validator.validateArkiverVariantRequest(request),
				"DokumentInfoId kan ikke være null");
	}

	@Test
	public void filIsNull() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET)
				.filType(FilTypeCode.PDF).build();

		assertThrows(UgyldigInputException.class, () ->
						validator.validateArkiverVariantRequest(request),
				"Fil kan ikke være null");
	}

	@Test
	public void filnavnIsNull() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.variant(VariantFormatCode.SLADDET)
				.filType(FilTypeCode.PDF).build();

		assertThrows(UgyldigInputException.class, () ->
						validator.validateArkiverVariantRequest(request),
				"Filnavn kan ikke være null");
	}

	@Test
	public void variantIsNull() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.filType(FilTypeCode.PDF).build();

		assertThrows(UgyldigInputException.class, () ->
						validator.validateArkiverVariantRequest(request),
				"Variant kan ikke være null");
	}

	@Test
	public void filTypeIsNull() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET).build();

		assertThrows(UgyldigInputException.class, () ->
						validator.validateArkiverVariantRequest(request),
				"Filtype kan ikke være null");
	}
}
package no.nav.dokarkiv.arkivervariant.rjoark102;

import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL;

import no.nav.dokarkiv.arkivervariant.exception.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;

public class ArkiverVariantValidatorTest {

	private ArkiverVariantValidator validator = new ArkiverVariantValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void happyPath() {
		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET.name())
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void dokumentIdIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("DokumentInfoId kan ikke være null");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET.name())
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void filIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Fil kan ikke være null");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET.name())
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void filnavnIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Filnavn kan ikke være null");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.variant(VariantFormatCode.SLADDET.name())
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void variantIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Variant kan ikke være null");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void filTypeIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Filtype kan ikke være null");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void variantErUgyldig() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Varianten: UGYLDIG er ugyldig");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant("UGYLDIG")
				.filType(FilTypeCode.PDF.name()).build();

		validator.validateArkiverVariantRequest(request);
	}

	@Test
	public void filTypeErUgyldig() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("Filtypen: UKJENT er ugyldig");

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(Base64.encodeBase64String(FIL))
				.filnavn("filnavn")
				.variant(VariantFormatCode.SLADDET.name())
				.filType("UKJENT").build();

		validator.validateArkiverVariantRequest(request);
	}
}
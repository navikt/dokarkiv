package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KasserDokumentValidatorTest {

	private final KasserDokumentValidator validator = new KasserDokumentValidator();

	@Test
	public void happyPath() {
		KasserDokumentRequest request = KasserDokumentRequest.builder()
				.dokumentInfoId(1L)
				.kassertAvNavn("Kassør")
				.build();

		assertDoesNotThrow(() -> validator.validerKasserDokumentRequest(request));
	}

	@Test
	public void throwExceptionWhenDokumentInfoIdIsNull() {
		KasserDokumentRequest request = KasserDokumentRequest.builder()
				.kassertAvNavn("Kassør")
				.build();

		assertThrows(UgyldigInputException.class, () -> validator.validerKasserDokumentRequest(request));
	}

	@Test
	public void throwExceptionWhenKassertAvNavnIsNull() {
		KasserDokumentRequest request = KasserDokumentRequest.builder()
				.dokumentInfoId(1L)
				.build();

		assertThrows(UgyldigInputException.class, () -> validator.validerKasserDokumentRequest(request));
	}
}
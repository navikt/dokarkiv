package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class KasserDokumentValidatorTest {

	private final KasserDokumentValidator validator = new KasserDokumentValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void happyPath() {
		KasserDokumentRequest request = KasserDokumentRequest.builder().dokumentInfoId(1L).kassertAvNavn("Kassør").build();
		validator.validerKasserDokumentRequest(request);
	}

	@Test
	public void throwExceptionWhenDokumentInfoIdIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("DokumentInfoId kan ikke være null");
		KasserDokumentRequest request = KasserDokumentRequest.builder().kassertAvNavn("Kassør").build();
		validator.validerKasserDokumentRequest(request);
	}

	@Test
	public void throwExceptionWhenKassertAvIsNull() {
		expectedException.expect(UgyldigInputException.class);
		expectedException.expectMessage("KassertAvNavn kan ikke være null");
		KasserDokumentRequest request = KasserDokumentRequest.builder().dokumentInfoId(1l).build();
		validator.validerKasserDokumentRequest(request);
	}
}
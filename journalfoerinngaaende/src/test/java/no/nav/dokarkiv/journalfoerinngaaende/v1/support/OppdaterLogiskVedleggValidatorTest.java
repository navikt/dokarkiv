package no.nav.dokarkiv.journalfoerinngaaende.v1.support;

import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class OppdaterLogiskVedleggValidatorTest {
	private final OppdaterLogiskVedleggValidator validator = new OppdaterLogiskVedleggValidator();

	@Test
	void shouldValidate() {
		validator.validate("1", "2", "3", createRequest());
	}

	@Test
	void shouldThrowExceptionWhenJournalpostIdNotSet() {
		assertThrows(InputValideringFeiletException.class, () -> {
			validator.validate("", "2", "3", createRequest());
		});
	}

	@Test
	void shouldThrowExceptionWhenDokumentInfoIdNotSet() {
		assertThrows(InputValideringFeiletException.class, () -> {
			validator.validate("1", "", "3", createRequest());
		});
	}

	@Test
	void shouldThrowExceptionWhenLogiskVedleggIdNotSet() {
		assertThrows(InputValideringFeiletException.class, () -> {
			validator.validate("1", "2", "", createRequest());
		});
	}

	@Test
	void shouldThrowExceptionWhenTittelLongerThan550() {
		assertThrows(InputValideringFeiletException.class, () -> {
			PutLogiskVedleggRequest request = createRequest();
			request.setTittel("GqKbuIgmUUPghiOEGndbVKViLTrBgidBgOuvmFbJBqkSLUXkcJqebCRjTsbkTPghImelqoNVguQrxNwOzaTPnMoQA" +
					"ZXwcHdTVJmXAcQLMUfizhHEDlpfiUDFeUDOANrlQEuXNRPwinTwWKHIiAmFhwyAcyJFWpVhbElPACeXblCMjgYgTTfIoDjyVbxY" +
					"hrBjQcVPPcGbIsiZfPgbonpKGkvByYiLivDKecpwNpsqLcXqbowxfCLGTgXlnHGQnSfcYcKWZbUrfVwgWmEKUHlmQPpjSRNRXIx" +
					"tROCFOcElKkObWIKynrHmwGicZARbsIyfORYszmalWjIIBmNBISTOBColhfWiVwSzsXyTXpQeJmiyEJWQBMFHgobkHYbdYcVyLS" +
					"vXeIqoaIQunMaRrFvVVNkcXfnskjYWfgeOKzlAJOwWJAfIvJbBcKTKgpDqLTwOytUJiqoGtbgUrDDVWQuhLfkLidfomSSiNeUyy" +
					"JRYhPTgNghXEXaMEyOziqJizKnwvMxZAedCtFiVaSeyRsyDbSZiwcueakjnSihJyJA");
			validator.validate("1", "2", "3", request);
		});
	}

	PutLogiskVedleggRequest createRequest() {
		PutLogiskVedleggRequest putLogiskVedleggRequest = new PutLogiskVedleggRequest();
		putLogiskVedleggRequest.setTittel("Hei verden");
		return putLogiskVedleggRequest;
	}
}
package no.nav.dokarkiv.journalfoerinngaaende.v1.map;


import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dokarkiv.core.exceptions.PostLogiskVedleggRequestValidationException;
import no.nav.dokarkiv.journalfoerinngaaende.v1.to.PostLogiskVedleggRequestTo;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.entities.SkannetInnhold.VEDLEGG_INNHOLD_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class PostLogiskVedleggRequestMapperTest {

	private final PostLogiskVedleggRequestMapper mapper = new PostLogiskVedleggRequestMapper();
	private static final String TITTEL = "PostLogiskVedlegg";

	@Test
	public void shouldThrowExceptionWhenTittelIsNull() {
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest(null);
		assertThrows(PostLogiskVedleggRequestValidationException.class, () ->
				mapper.map(request), "Tittelen kan ikke være null eller tom");
	}

	@Test
	public void shouldThrowExceptionWhenTittelIsBlank() {
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest("");
		assertThrows(PostLogiskVedleggRequestValidationException.class, () ->
				mapper.map(request), "Tittelen kan ikke være null eller tom");
	}

	@Test
	public void shouldThrowExceptionWhenTittelIsLongerThan550Chars() {
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest("GqKbuIgmUUPghiOEGndbVKViLTrBgidBgOuvmFbJBqkSLUXkcJqebCRjTsbkTPghImelqoNVguQrxNwOzaTPnMoQA" +
				"ZXwcHdTVJmXAcQLMUfizhHEDlpfiUDFeUDOANrlQEuXNRPwinTwWKHIiAmFhwyAcyJFWpVhbElPACeXblCMjgYgTTfIoDjyVbxY" +
				"hrBjQcVPPcGbIsiZfPgbonpKGkvByYiLivDKecpwNpsqLcXqbowxfCLGTgXlnHGQnSfcYcKWZbUrfVwgWmEKUHlmQPpjSRNRXIx" +
				"tROCFOcElKkObWIKynrHmwGicZARbsIyfORYszmalWjIIBmNBISTOBColhfWiVwSzsXyTXpQeJmiyEJWQBMFHgobkHYbdYcVyLS" +
				"vXeIqoaIQunMaRrFvVVNkcXfnskjYWfgeOKzlAJOwWJAfIvJbBcKTKgpDqLTwOytUJiqoGtbgUrDDVWQuhLfkLidfomSSiNeUyy" +
				"JRYhPTgNghXEXaMEyOziqJizKnwvMxZAedCtFiVaSeyRsyDbSZiwcueakjnSihJyJA");
		assertThrows(PostLogiskVedleggRequestValidationException.class, () ->
				mapper.map(request), "Tittelen kan ikke være lengre enn " + VEDLEGG_INNHOLD_LENGTH + " tegn.");
	}

	@Test
	public void shouldMapRequest() {
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest(TITTEL);
		PostLogiskVedleggRequestTo requestTo = mapper.map(request);
		assertThat(requestTo.getTittel()).isEqualTo(TITTEL);
	}

	private PostLogiskVedleggRequest createPostLogiskVedleggRequest(String tittel) {
		PostLogiskVedleggRequest request = new PostLogiskVedleggRequest();
		request.setTittel(tittel);
		return request;
	}
}

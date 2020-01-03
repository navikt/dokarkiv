package no.nav.dokarkiv.journalfoerinngaaende.v1.map;


import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dokarkiv.core.exceptions.PostLogiskVedleggRequestValidationException;
import no.nav.dokarkiv.journalfoerinngaaende.v1.to.PostLogiskVedleggRequestTo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class PostLogiskVedleggRequestMapperTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Inject
	private PostLogiskVedleggRequestMapper mapper;
	private static final String TITTEL = "PostLogiskVedlegg";

	@Before
	public void setUp(){
		mapper = new PostLogiskVedleggRequestMapper();
	}


	@Test
	public void postLogiskVeleggRequestMapperShouldThrowsExceptionWhenTittlenErNull(){
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest(null);
		expectedException.expect(PostLogiskVedleggRequestValidationException.class);
		expectedException.expectMessage("Tittelen kan ikke være null eller tom");

		mapper.map(request);
	}

	@Test
	public void postLogiskVeleggRequestMapperShouldThrowsExceptionWhenTittlenErBlank(){
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest("");
		expectedException.expect(PostLogiskVedleggRequestValidationException.class);
		expectedException.expectMessage("Tittelen kan ikke være null eller tom");

		mapper.map(request);
	}

	@Test
	public void postLogiskVeleggRequestMapperShouldMappTittlen(){
		PostLogiskVedleggRequest request = createPostLogiskVedleggRequest(TITTEL);
		PostLogiskVedleggRequestTo requestTo = mapper.map(request);
		assertThat(requestTo.getTittel(),is(TITTEL));
	}


	private PostLogiskVedleggRequest createPostLogiskVedleggRequest(String tittel) {
		PostLogiskVedleggRequest request = new PostLogiskVedleggRequest();
		request.setTittel(tittel);
		return request;
	}


}

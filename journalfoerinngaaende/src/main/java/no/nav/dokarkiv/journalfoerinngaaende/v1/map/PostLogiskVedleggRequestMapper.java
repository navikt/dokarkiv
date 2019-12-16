package no.nav.dokarkiv.journalfoerinngaaende.v1.map;

import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.PostLogiskVedleggRequestValidationException;
import no.nav.dokarkiv.journalfoerinngaaende.v1.to.PostLogiskVedleggRequestTo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PostLogiskVedleggRequestMapper {

	public PostLogiskVedleggRequestTo map(PostLogiskVedleggRequest request){
		validateInput(request);
		return PostLogiskVedleggRequestTo.builder()
				.tittel(request.getTittel())
				.build();
	}

	private void validateInput(PostLogiskVedleggRequest request) throws InputValideringFeiletException {
		if(StringUtils.isBlank(request.getTittel())){
			throw new PostLogiskVedleggRequestValidationException("Tittelen kan ikke være null eller tom");
		}
	}
}

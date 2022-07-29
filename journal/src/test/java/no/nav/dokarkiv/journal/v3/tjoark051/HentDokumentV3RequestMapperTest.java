package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit test for {@link HentDokumentV3RequestMapper}
 *
 * @author Roar Bjurstrøm
 */
public class HentDokumentV3RequestMapperTest {
	private static final String JOURNALPOST_ID = "1";
	private static final String DOKUMENT_ID = "42";

	private HentDokumentV3RequestMapper mapper = new HentDokumentV3RequestMapper();

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		HentDokumentRequest wsRequest = new HentDokumentRequest();
		wsRequest.setJournalpostId(JOURNALPOST_ID);
		wsRequest.setDokumentId(DOKUMENT_ID);
		Variantformater variantformater = new Variantformater();
		variantformater.setValue(VariantFormatCode.ARKIV.name());
		wsRequest.setVariantformat(variantformater);

		HentDokumentRequestTo domainRequest = mapper.map(wsRequest);
		assertThat(domainRequest.getJournalpostId(), is(Long.valueOf(JOURNALPOST_ID)));
		assertThat(domainRequest.getDokumentInfoId(), is(Long.valueOf(DOKUMENT_ID)));
		assertThat(domainRequest.getVariantFormat(), is(VariantFormatCode.ARKIV));
	}

	@Test
	public void shouldThrowExceptionWhenMappingJournalpostIdIsNotANumber() {
		HentDokumentRequest invalidWsRequest = new HentDokumentRequest();
		invalidWsRequest.setJournalpostId("not a valid journalpostId");

		assertThrows(NumberFormatException.class, () -> mapper.map(invalidWsRequest));
	}
}

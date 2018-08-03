package no.nav.dokarkiv.journal.v3.tjoark050;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import org.junit.Test;

public class HentDokumentURLV3RequestMapperTest {

	private static final String JOURNALPOST_ID = "1";
	private static final String DOKUMENT_ID = "42";

	private HentDokumentURLV3RequestMapper mapper = new HentDokumentURLV3RequestMapper();

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		HentDokumentURLRequest wsRequest = new HentDokumentURLRequest();
		wsRequest.setJournalpostId(JOURNALPOST_ID);
		wsRequest.setDokumentId(DOKUMENT_ID);
		Variantformater variantformater = new Variantformater();
		variantformater.setValue(VariantFormatCode.ARKIV.name());
		wsRequest.setVariantformat(variantformater);

		HentDokumentUrlRequestTo domainRequest = mapper.map(wsRequest);
		assertThat(domainRequest.getJournalpostId(), is(Long.valueOf(JOURNALPOST_ID)));
		assertThat(domainRequest.getDokumentInfoId(), is(Long.valueOf(DOKUMENT_ID)));
		assertThat(domainRequest.getVariantFormat(), is(VariantFormatCode.ARKIV));
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExcpetionWhenMapping_journalpostIdIsNotANumber() {
		HentDokumentURLRequest invalidWsRequest = new HentDokumentURLRequest();
		invalidWsRequest.setJournalpostId("not a valid journalpostId");
		mapper.map(invalidWsRequest);
	}

}
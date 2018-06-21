package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class ArkiverVedleggResponseMapperTest {

	public static final Long JOURNALPOST_ID = 200L;
	public static final Long DOKUMENT_INFO_ID = 124L;

	private ArkiverVedleggResponseMapper responseMapper;

	@Before
	public void setUp() {
		responseMapper = new ArkiverVedleggResponseMapper();
	}

	@Test
	public void shouldMapArkiverVedleggResponse() {
		ArkiverVedleggResponseTo responseTo = ArkiverVedleggResponseTo.create(JOURNALPOST_ID, DOKUMENT_INFO_ID);
		ArkiverVedleggResponse reponse = responseMapper.map(responseTo);
		assertThat(reponse.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(responseTo.getDokumentInfoId(), is(DOKUMENT_INFO_ID));
	}
}
package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class ArkiverVedleggResponseMapperTest {

	public static final Long JOURNALPOST_ID = 200L;
	public static final Long DOKUMENT_INFO_ID = 124L;

	private ArkiverVedleggResponseMapper responseMapper;

	@BeforeEach
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
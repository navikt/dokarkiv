package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for DefaultHentJournalOgDokumentStatusResponseMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultHentJournalOgDokumentStatusResponseMapperTest {

	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final Long METAFORCE_INSTANCE_ID = 500L;

	private DefaultHentJournalOgDokumentStatusResponseMapper responseMapper;

	@BeforeEach
	public void setUp() throws Exception {
		responseMapper = new DefaultHentJournalOgDokumentStatusResponseMapper();
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		HentJournalOgDokumentStatusResponseTo domainResponse = new HentJournalOgDokumentStatusResponseTo(JOURNAL_STATUS,
				DOKUMENT_STATUS, METAFORCE_INSTANCE_ID);

		HentJournalOgDokumentStatusResponse wsResponse = responseMapper.map(domainResponse);

		assertThat(wsResponse.getJournalStatus(), is(JOURNAL_STATUS.name()));
		assertThat(wsResponse.getDokumentStatus(), is(DOKUMENT_STATUS.name()));
		assertThat(wsResponse.getMetaForceInstanceId(), is(METAFORCE_INSTANCE_ID));
	}

}

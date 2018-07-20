package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Unit tests for {@link IdentifiserJournalpostV2RequestMapper}
 *
 * @author Ketill Fenne, Visma Consulting.
 *
 */
public class IdentifiserJournalpostRequestMapperTest {

	public static final String KANAL_REFERANSE_ID = "KanalRefId";
	public static final MottaksKanalCode MOTTAK_KANAL = MottaksKanalCode.NAV_NO;
	public static final String REQUEST_MOTTAK_KANAL = "NAV_NO";

	private IdentifiserJournalpostV2RequestMapper mapper;

	@Before
	public void setUp() throws Exception {
		mapper = new IdentifiserJournalpostV2RequestMapper();
	}

	@Test
	public void shouldMap() {
		IdentifiserJournalpostToRequest requestTo = mapper.map(createRequest());
		assertThat(requestTo.getKanalReferanseId(), is(KANAL_REFERANSE_ID));
		assertThat(requestTo.getMottaksKanal(), is(MOTTAK_KANAL));
	}

	private IdentifiserJournalpostRequest createRequest() {
		IdentifiserJournalpostRequest request = new IdentifiserJournalpostRequest();
		request.setKanalReferanseId(KANAL_REFERANSE_ID);
		request.setMottakskanal(REQUEST_MOTTAK_KANAL);
		return request;
	}
}
package no.nav.dokarkiv.core.consumer.aktoer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

/**
 * Unit test class for {@link HentIdentForAktoerIdResponseMapper}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentIdentForAktoerIdResponseMapperTest {

	private static final String IDENT = "ident";

	private HentIdentForAktoerIdResponseMapper mapper = new HentIdentForAktoerIdResponseMapper();

	@Test
	public void shouldMapAktoer() throws Exception {
		HentIdentForAktoerIdResponseTo responseTo = mapper.map(createHentAktoerIdForIdentResponse());

		assertThat(responseTo.getIdent(), is(IDENT));
	}

	private HentIdentForAktoerIdResponse createHentAktoerIdForIdentResponse() {
		HentIdentForAktoerIdResponse wsResponse = new HentIdentForAktoerIdResponse();
		wsResponse.setIdent(IDENT);
		return wsResponse;
	}
}
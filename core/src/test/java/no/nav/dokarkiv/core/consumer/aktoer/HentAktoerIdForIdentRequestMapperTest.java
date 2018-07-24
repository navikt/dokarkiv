package no.nav.dokarkiv.core.consumer.aktoer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link HentAktoerIdForIdentRequestMapper}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentAktoerIdForIdentRequestMapperTest {

	private static final String IDENT = "ident";
	private HentAktoerIdForIdentRequestMapper requestMapper = new HentAktoerIdForIdentRequestMapper();

	@Test
	public void shouldMap() {
		assertThat(requestMapper.map(new HentAktoerIdForIdentRequestTo(IDENT)).getIdent(), is(IDENT));
	}

}
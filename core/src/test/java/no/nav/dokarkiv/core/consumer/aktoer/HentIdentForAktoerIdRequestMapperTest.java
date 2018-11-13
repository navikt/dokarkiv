package no.nav.dokarkiv.core.consumer.aktoer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link HentAktoerIdForIdentRequestMapper}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentIdentForAktoerIdRequestMapperTest {

	private static final String AKTOERID = "aktoerId";
	private HentIdentForAktoerIdRequestMapper aktoerIdRequestMapper = new HentIdentForAktoerIdRequestMapper();

	@Test
	public void shouldMapAktoer() {
		assertThat(aktoerIdRequestMapper.map(new HentIdentForAktoerIdRequestTo(AKTOERID)).getAktoerId(), is(AKTOERID));
	}
}
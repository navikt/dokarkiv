package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class GsakCteMapperTest {

	private final GsakCteMapper gsakCteMapper = new GsakCteMapper();

	@Test
	public void shouldNotMapToGsakerCteWhenEmptyGsaker() {
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(new ArrayList<>(), false);
		assertFalse(gsakCte.isGsakerExists());
		assertThat(gsakCte.getCteSql(), is(""));
		assertThat(gsakCte.getGsakIdParams().size(), is(0));
	}

	@Test
	public void shouldGsakerCteWhenGsakerExists() {
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(Arrays.asList("1", "2"), false);
		assertTrue(gsakCte.isGsakerExists());
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds0"));
		assertThat(gsakCte.getCteSql(), is(
				"     gsaksaker AS\n" +
						"       (SELECT s.journalpost_id\n" +
						"        FROM t_saksrelasjon s\n" +
						"        WHERE (s.k_fagsystem = 'FS22' AND s.sak_nr_fk IN (:gsakIds0))\n" +
						"          AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"       ),\n"));
	}

	@Test
	public void shouldGsakerCteWhenGsakerExistsAndOnlyListFeilregistrerte() {
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(Arrays.asList("1", "2"), true);
		assertTrue(gsakCte.isGsakerExists());
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds0"));
		assertThat(gsakCte.getCteSql(), is(
				"     gsaksaker AS\n" +
						"       (SELECT s.journalpost_id\n" +
						"        FROM t_saksrelasjon s\n" +
						"        WHERE (s.k_fagsystem = 'FS22' AND s.sak_nr_fk IN (:gsakIds0))\n" +
						"          AND (s.feilregistrert = 1)\n" +
						"       ),\n"));
	}

	@Test
	public void shouldMapGsakCteWhenMoreThan1000() {
		List<String> gsakIds = IntStream.range(0, 1001).mapToObj(Objects::toString).collect(Collectors.toList());
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(gsakIds, false);
		assertTrue(gsakCte.isGsakerExists());
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds0"));
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds1"));
		assertThat(gsakCte.getCteSql(), is(
				"     gsaksaker AS\n" +
						"       (SELECT s.journalpost_id\n" +
						"        FROM t_saksrelasjon s\n" +
						"        WHERE (s.k_fagsystem = 'FS22' AND (s.sak_nr_fk IN (:gsakIds0) OR s.sak_nr_fk IN (:gsakIds1)))\n" +
						"          AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"       ),\n"));
	}
}
package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
				"""
						     gsaksaker AS
						       (SELECT s.journalpost_id
						        FROM t_saksrelasjon s
						        WHERE (s.k_fagsystem = 'FS22' AND s.sak_id IN (:gsakIds0))
						          AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))
						       ),
						"""));
	}

	@Test
	public void shouldGsakerCteWhenGsakerExistsAndOnlyListFeilregistrerte() {
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(Arrays.asList("1", "2"), true);
		assertTrue(gsakCte.isGsakerExists());
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds0"));
		assertThat(gsakCte.getCteSql(), is(
				"""
						     gsaksaker AS
						       (SELECT s.journalpost_id
						        FROM t_saksrelasjon s
						        WHERE (s.k_fagsystem = 'FS22' AND s.sak_id IN (:gsakIds0))
						          AND (s.feilregistrert = 1)
						       ),
						"""));
	}

	@Test
	public void shouldMapGsakCteWhenMoreThan1000() {
		List<String> gsakIds = IntStream.range(0, 1001).mapToObj(Objects::toString).collect(Collectors.toList());
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(gsakIds, false);
		assertTrue(gsakCte.isGsakerExists());
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds0"));
		assertTrue(gsakCte.getGsakIdParams().containsKey("gsakIds1"));
		assertThat(gsakCte.getCteSql(), is(
				"""
						     gsaksaker AS
						       (SELECT s.journalpost_id
						        FROM t_saksrelasjon s
						        WHERE (s.k_fagsystem = 'FS22' AND (s.sak_id IN (:gsakIds0) OR s.sak_id IN (:gsakIds1)))
						          AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))
						       ),
						"""));
	}
}
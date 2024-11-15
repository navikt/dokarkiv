package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.hentjournalsakinfo.JournalpostFilter;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.dokarkiv.hentjournalsakinfo.common.PadUtils.inPaddingBase2;
import static no.nav.dokarkiv.hentjournalsakinfo.common.PadUtils.inPaddingFixed3;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalpostSqlGenerator.finnJournalposterSql;

@Slf4j
@Repository
class FinnJournalposterSpringJdbcRepository {
	private static final List<Long> NOT_USED_NUMBER = Collections.singletonList(-999L);
	private static final List<String> NOT_USED_TEXT = Collections.singletonList("notused");
	private static final List<String> ALL_JOURNALSTATUS = Stream.of(JournalStatusCode.values()).map(Enum::name).collect(Collectors.toList());
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Arrays.asList(false, false); // in clause padding
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	private static final String PSAK_IDS_PARAM = "psakIds";
	private static final String CTE_ALIAS_GSAKSAKER = "gsaksaker";
	private static final String CTE_ALIAS_PSAKSAKER = "psaksaker";
	private static final String CTE_ALIAS_MIDLERTIDIGE = "midlertidige";
	private static final int ORACLE_PARALLELL = 150;
	private final GsakCteMapper gsakCteMapper;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public FinnJournalposterSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.gsakCteMapper = new GsakCteMapper();
		this.jdbcTemplate = jdbcTemplate;
	}

	List<JournalpostDto> finnJournalposter(final List<String> gsakIds,
										   final List<String> psakIds,
										   final JournalpostFilter journalpostFilter) {
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(gsakIds, journalpostFilter.isKunFeilregistrerte());
		List<String> cteAliases = buildCteAliases(psakIds, journalpostFilter, gsakCte);
		MapSqlParameterSource namedParams = buildNamedParams(psakIds, journalpostFilter, gsakCte);

		if (cteAliases.isEmpty()) {
			return new ArrayList<>();
		}
		boolean parallell = (gsakIds.size() > ORACLE_PARALLELL);
		String finnJournalposterSql = finnJournalposterSql(journalpostFilter, cteAliases, gsakCte.getCteSql(), parallell);
		FinnJournalposterRowCallbackHandler finnJournalposterRowCallbackHandler = new FinnJournalposterRowCallbackHandler();
		jdbcTemplate.query(finnJournalposterSql, namedParams, finnJournalposterRowCallbackHandler);
		return finnJournalposterRowCallbackHandler.getJournalpostDtos();
	}

	private MapSqlParameterSource buildNamedParams(final List<String> psakIds, final JournalpostFilter journalpostFilter, final GsakCteMapper.GsakCte gsakCte) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValues(gsakCte.getGsakIdParams());
		if (psakIds == null || psakIds.isEmpty()) {
			namedParams.addValue(PSAK_IDS_PARAM, NOT_USED_NUMBER);
		} else {
			namedParams.addValue(PSAK_IDS_PARAM, inPaddingBase2(psakIds));
		}
		if (journalpostFilter.isInkluderMidlertidigeJournalposter()) {
			namedParams.addValue("alleIdenter", inPaddingBase2(journalpostFilter.getAlleIdenter()));
		} else {
			namedParams.addValue("alleIdenter", NOT_USED_TEXT);
		}
		if (journalpostFilter.isKunFeilregistrerte()) {
			namedParams.addValue("inkluderJournalStatus", NOT_USED_TEXT);
		} else {
			namedParams.addValue("inkluderJournalStatus", inPaddingBase2(journalpostFilter.getInkluderJournalStatus()));
		}

		namedParams.addValue("fraDato", Timestamp.valueOf(journalpostFilter.getFraDato().atStartOfDay()));
		if (journalpostFilter.getTilDato() != null) {
			namedParams.addValue("tilDato", Timestamp.valueOf(journalpostFilter.getTilDato().atStartOfDay()));
		}
		namedParams.addValue("inkluderJournalpostType", inPaddingFixed3(journalpostFilter.getInkluderJournalpostType()));
		namedParams.addValue("allJournalStatus", ALL_JOURNALSTATUS);
		namedParams.addValue("visFeilregistrert", journalpostFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST);
		namedParams.addValue("antallRader", journalpostFilter.getAntallRader());
		namedParams.addValue("journalpostIdPeker", journalpostFilter.getJournalpostIdPeker());
		return namedParams;
	}

	private List<String> buildCteAliases(final List<String> psakIds, final JournalpostFilter journalpostFilter, final GsakCteMapper.GsakCte gsakCte) {
		List<String> cteAliases = new ArrayList<>();
		if (gsakCte.isGsakerExists()) {
			cteAliases.add(CTE_ALIAS_GSAKSAKER);
		}
		if (psakIds != null && !psakIds.isEmpty()) {
			cteAliases.add(CTE_ALIAS_PSAKSAKER);
		}
		if (journalpostFilter.isInkluderMidlertidigeJournalposter()) {
			cteAliases.add(CTE_ALIAS_MIDLERTIDIGE);
		}
		return cteAliases;
	}
}

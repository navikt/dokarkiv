package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalpostSqlGenerator.finnJournalposterSql;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class FinnJournalposterSpringJdbcRepository {
	private static final ResultSetExtractor<List<JournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "dokumenter_dokumentinfoid", "dokumenter_logiske_tittel")
			.newResultSetExtractor(JournalpostDto.class);
	private static final List<String> NOT_USED = Collections.singletonList("notused");
	private static final List<String> ALL_JOURNALSTATUS = Stream.of(JournalStatusCode.values()).map(Enum::name).collect(Collectors.toList());
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Collections.singletonList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	private static final String PSAK_IDS_PARAM = "psakIds";

	private final GsakCteMapper gsakCteMapper;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	public FinnJournalposterSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.gsakCteMapper = new GsakCteMapper();
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<JournalpostDto> finnJournalposter(final List<String> gsakIds,
												  final List<String> psakIds,
												  final JournalpostFilter journalpostFilter) {
		List<String> cteAliases = new ArrayList<>();
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(gsakIds, journalpostFilter.isKunFeilregistrerte());
		namedParams.addValues(gsakCte.getGsakIdParams());
		if (gsakCte.isGsakerExists()) {
			cteAliases.add("gsaksaker");
		}
		if (psakIds == null || psakIds.isEmpty()) {
			namedParams.addValue(PSAK_IDS_PARAM, NOT_USED);
		} else {
			namedParams.addValue(PSAK_IDS_PARAM, psakIds);
			cteAliases.add("psaksaker");
		}
		if (journalpostFilter.isInkluderMidlertidigeJournalposter()) {
			namedParams.addValue("alleIdenter", journalpostFilter.getAlleIdenter());
			cteAliases.add("midlertidige");
		} else {
			namedParams.addValue("alleIdenter", NOT_USED);
		}
		if (journalpostFilter.isKunFeilregistrerte()) {
			namedParams.addValue("inkluderJournalStatus", NOT_USED);
		} else {
			namedParams.addValue("inkluderJournalStatus", journalpostFilter.getInkluderJournalStatus());
		}

		namedParams.addValue("fraDato", Timestamp.valueOf(journalpostFilter.getFraDato().atStartOfDay()));
		namedParams.addValue("inkluderTema", journalpostFilter.getInkluderTema());
		namedParams.addValue("inkluderJournalpostType", journalpostFilter.getInkluderJournalpostType());
		namedParams.addValue("allJournalStatus", ALL_JOURNALSTATUS);
		namedParams.addValue("visFeilregistrert", journalpostFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST);
		namedParams.addValue("antallRader", journalpostFilter.getAntallRader());
		namedParams.addValue("journalpostIdPeker", journalpostFilter.getJournalpostIdPeker());

		if (cteAliases.isEmpty()) {
			return new ArrayList<>();
		}

		String finnJournalposterSql = finnJournalposterSql(journalpostFilter, cteAliases, gsakCte.getCteSql());
		return jdbcTemplate.query(finnJournalposterSql, namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);
	}
}
